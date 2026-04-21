package imarouter

import (
	"bufio"
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"time"
)

const DefaultBaseURL = "https://api.imarouter.com"

type ClientOption func(*Client)

type APIError struct {
	StatusCode int
	Message    string
	Body       any
}

func (e *APIError) Error() string {
	return e.Message
}

type SSEEvent struct {
	Event string
	Data  json.RawMessage
}

type Client struct {
	BaseURL        string
	APIKey         string
	HTTPClient     *http.Client
	defaultHeaders http.Header

	ChatCompletions *ChatCompletionsService
	Responses       *ResponsesService
	Messages        *MessagesService
	Images          *ImagesService
	Videos          *VideosService
	Kling           *KlingService
	Midjourney      *MidjourneyService
}

func WithBaseURL(baseURL string) ClientOption {
	return func(c *Client) {
		if strings.TrimSpace(baseURL) != "" {
			c.BaseURL = strings.TrimRight(baseURL, "/")
		}
	}
}

func WithTimeout(timeout time.Duration) ClientOption {
	return func(c *Client) {
		if timeout > 0 {
			c.HTTPClient.Timeout = timeout
		}
	}
}

func WithHTTPClient(client *http.Client) ClientOption {
	return func(c *Client) {
		if client != nil {
			c.HTTPClient = client
		}
	}
}

func WithHeader(key, value string) ClientOption {
	return func(c *Client) {
		if strings.TrimSpace(key) == "" {
			return
		}
		c.defaultHeaders.Set(key, value)
	}
}

func NewClient(apiKey string, opts ...ClientOption) (*Client, error) {
	if strings.TrimSpace(apiKey) == "" {
		apiKey = os.Getenv("IMA_API_KEY")
	}
	if strings.TrimSpace(apiKey) == "" {
		return nil, errors.New("api key is required")
	}

	client := &Client{
		BaseURL:    strings.TrimRight(envOr("IMA_BASE_URL", DefaultBaseURL), "/"),
		APIKey:     apiKey,
		HTTPClient: &http.Client{Timeout: 60 * time.Second},
		defaultHeaders: http.Header{
			"Authorization": []string{"Bearer " + apiKey},
			"Content-Type":  []string{"application/json"},
		},
	}

	for _, opt := range opts {
		opt(client)
	}

	if client.BaseURL == "" {
		client.BaseURL = DefaultBaseURL
	}

	client.ChatCompletions = &ChatCompletionsService{client: client}
	client.Responses = &ResponsesService{client: client}
	client.Messages = &MessagesService{client: client}
	client.Images = &ImagesService{client: client}
	client.Videos = &VideosService{client: client}
	client.Kling = &KlingService{client: client}
	client.Midjourney = &MidjourneyService{client: client}

	return client, nil
}

func (c *Client) CloseIdleConnections() {
	if c.HTTPClient != nil {
		c.HTTPClient.CloseIdleConnections()
	}
}

func (c *Client) buildURL(path string) string {
	normalized := path
	if !strings.HasPrefix(normalized, "/") {
		normalized = "/" + normalized
	}
	if strings.HasSuffix(c.BaseURL, "/v1") && strings.HasPrefix(normalized, "/v1/") {
		normalized = normalized[3:]
	}
	return c.BaseURL + normalized
}

func (c *Client) newRequest(
	ctx context.Context,
	method string,
	path string,
	body any,
	headers http.Header,
) (*http.Request, error) {
	var reader io.Reader
	if body != nil {
		raw, err := json.Marshal(body)
		if err != nil {
			return nil, err
		}
		reader = bytes.NewReader(raw)
	}

	req, err := http.NewRequestWithContext(ctx, method, c.buildURL(path), reader)
	if err != nil {
		return nil, err
	}

	for key, values := range c.defaultHeaders {
		for _, value := range values {
			req.Header.Add(key, value)
		}
	}

	for key, values := range headers {
		req.Header.Del(key)
		for _, value := range values {
			req.Header.Add(key, value)
		}
	}

	return req, nil
}

func (c *Client) doJSON(
	ctx context.Context,
	method string,
	path string,
	body any,
	headers http.Header,
	out any,
) error {
	req, err := c.newRequest(ctx, method, path, body, headers)
	if err != nil {
		return err
	}

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode >= http.StatusBadRequest {
		return parseAPIError(resp)
	}

	if out == nil {
		io.Copy(io.Discard, resp.Body)
		return nil
	}

	return json.NewDecoder(resp.Body).Decode(out)
}

func (c *Client) doBinary(
	ctx context.Context,
	method string,
	path string,
	body any,
	headers http.Header,
) ([]byte, error) {
	req, err := c.newRequest(ctx, method, path, body, headers)
	if err != nil {
		return nil, err
	}

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode >= http.StatusBadRequest {
		return nil, parseAPIError(resp)
	}

	return io.ReadAll(resp.Body)
}

func (c *Client) doSSE(
	ctx context.Context,
	method string,
	path string,
	body any,
	headers http.Header,
	handler func(SSEEvent) error,
) error {
	if handler == nil {
		return errors.New("sse handler is required")
	}

	if headers == nil {
		headers = http.Header{}
	}
	headers.Set("Accept", "text/event-stream")

	req, err := c.newRequest(ctx, method, path, body, headers)
	if err != nil {
		return err
	}

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode >= http.StatusBadRequest {
		return parseAPIError(resp)
	}

	scanner := bufio.NewScanner(resp.Body)
	scanner.Buffer(make([]byte, 0, 64*1024), 1024*1024)

	var (
		eventName string
		dataLines []string
	)

	flush := func() error {
		if len(dataLines) == 0 {
			eventName = ""
			return nil
		}

		payload := strings.Join(dataLines, "\n")
		dataLines = nil

		if payload == "[DONE]" {
			return io.EOF
		}

		if err := handler(SSEEvent{
			Event: eventName,
			Data:  json.RawMessage(payload),
		}); err != nil {
			return err
		}

		eventName = ""
		return nil
	}

	for scanner.Scan() {
		line := scanner.Text()
		if line == "" {
			if err := flush(); err != nil {
				if errors.Is(err, io.EOF) {
					return nil
				}
				return err
			}
			continue
		}

		switch {
		case strings.HasPrefix(line, ":"):
			continue
		case strings.HasPrefix(line, "event:"):
			eventName = strings.TrimSpace(line[len("event:"):])
		case strings.HasPrefix(line, "data:"):
			dataLines = append(dataLines, strings.TrimLeft(line[len("data:"):], " "))
		}
	}

	if err := scanner.Err(); err != nil {
		return err
	}

	if err := flush(); err != nil && !errors.Is(err, io.EOF) {
		return err
	}

	return nil
}

func parseAPIError(resp *http.Response) error {
	bodyBytes, _ := io.ReadAll(resp.Body)
	if len(bodyBytes) == 0 {
		return &APIError{
			StatusCode: resp.StatusCode,
			Message:    fmt.Sprintf("IMA Router request failed with status %d", resp.StatusCode),
		}
	}

	var body any
	_ = json.Unmarshal(bodyBytes, &body)

	message := strings.TrimSpace(string(bodyBytes))
	if parsed := errorMessageFromBody(body); parsed != "" {
		message = parsed
	}

	return &APIError{
		StatusCode: resp.StatusCode,
		Message:    message,
		Body:       body,
	}
}

func errorMessageFromBody(body any) string {
	payload, ok := body.(map[string]any)
	if !ok {
		return ""
	}

	if rawError, ok := payload["error"]; ok {
		switch typed := rawError.(type) {
		case string:
			return typed
		case map[string]any:
			if message, ok := typed["message"].(string); ok && message != "" {
				return message
			}
			if valueType, ok := typed["type"].(string); ok && valueType != "" {
				return valueType
			}
		}
	}

	if message, ok := payload["message"].(string); ok && message != "" {
		return message
	}

	return ""
}

func envOr(key, fallback string) string {
	if value := strings.TrimSpace(os.Getenv(key)); value != "" {
		return value
	}
	return fallback
}

func waitForTask[T any](
	ctx context.Context,
	taskID string,
	timeout time.Duration,
	interval time.Duration,
	fetch func(context.Context, string) (*T, error),
	isTerminal func(*T) bool,
	onPoll func(*T, int),
) (*T, error) {
	if timeout <= 0 {
		timeout = 30 * time.Minute
	}
	if interval <= 0 {
		interval = 5 * time.Second
	}

	deadline := time.Now().Add(timeout)
	attempt := 0

	for {
		if err := ctx.Err(); err != nil {
			return nil, err
		}

		result, err := fetch(ctx, taskID)
		if err != nil {
			return nil, err
		}

		attempt++
		if onPoll != nil {
			onPoll(result, attempt)
		}

		if isTerminal(result) {
			return result, nil
		}

		if time.Now().After(deadline) {
			return nil, fmt.Errorf("timed out while waiting for task %s", taskID)
		}

		timer := time.NewTimer(interval)
		select {
		case <-ctx.Done():
			timer.Stop()
			return nil, ctx.Err()
		case <-timer.C:
		}
	}
}
