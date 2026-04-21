package imarouter

import (
	"context"
	"encoding/json"
	"net/http"
	"strings"
)

const DefaultAnthropicVersion = "2023-06-01"

type ChatMessage struct {
	Role    string `json:"role"`
	Content string `json:"content"`
}

type AnthropicMessage struct {
	Role    string `json:"role"`
	Content string `json:"content"`
}

type FunctionDefinition struct {
	Name        string         `json:"name"`
	Description string         `json:"description,omitempty"`
	Parameters  map[string]any `json:"parameters,omitempty"`
}

type Tool struct {
	Type     string             `json:"type"`
	Function FunctionDefinition `json:"function"`
}

type ToolChoiceFunction struct {
	Name string `json:"name"`
}

type ToolChoiceObject struct {
	Type     string              `json:"type"`
	Function *ToolChoiceFunction `json:"function,omitempty"`
}

type ChatCompletionRequest struct {
	Model       string        `json:"model"`
	Messages    []ChatMessage `json:"messages"`
	Stream      bool          `json:"stream,omitempty"`
	Temperature *float64      `json:"temperature,omitempty"`
	MaxTokens   *int          `json:"max_tokens,omitempty"`
	Tools       []Tool        `json:"tools,omitempty"`
	ToolChoice  any           `json:"tool_choice,omitempty"`
}

type ChatCompletionFunctionCall struct {
	Name      string `json:"name,omitempty"`
	Arguments string `json:"arguments,omitempty"`
}

type ChatCompletionToolCall struct {
	ID       string                     `json:"id,omitempty"`
	Type     string                     `json:"type,omitempty"`
	Function ChatCompletionFunctionCall `json:"function,omitempty"`
}

type ChatCompletionMessageContent struct {
	Role      string                   `json:"role,omitempty"`
	Content   string                   `json:"content,omitempty"`
	ToolCalls []ChatCompletionToolCall `json:"tool_calls,omitempty"`
}

type ChatCompletionChoice struct {
	Index        int                          `json:"index,omitempty"`
	Message      ChatCompletionMessageContent `json:"message"`
	FinishReason string                       `json:"finish_reason,omitempty"`
}

type ChatCompletionUsage struct {
	PromptTokens     int `json:"prompt_tokens,omitempty"`
	CompletionTokens int `json:"completion_tokens,omitempty"`
	TotalTokens      int `json:"total_tokens,omitempty"`
}

type ChatCompletionResponse struct {
	ID      string                 `json:"id,omitempty"`
	Object  string                 `json:"object,omitempty"`
	Model   string                 `json:"model,omitempty"`
	Choices []ChatCompletionChoice `json:"choices,omitempty"`
	Usage   ChatCompletionUsage    `json:"usage,omitempty"`
}

func (r *ChatCompletionResponse) Text() string {
	if r == nil || len(r.Choices) == 0 {
		return ""
	}
	return r.Choices[0].Message.Content
}

type ChatCompletionDelta struct {
	Role    string `json:"role,omitempty"`
	Content string `json:"content,omitempty"`
}

type ChatCompletionChunkChoice struct {
	Index        int                          `json:"index,omitempty"`
	Delta        ChatCompletionDelta          `json:"delta"`
	Message      ChatCompletionMessageContent `json:"message"`
	FinishReason string                       `json:"finish_reason,omitempty"`
}

type ChatCompletionChunk struct {
	ID      string                      `json:"id,omitempty"`
	Object  string                      `json:"object,omitempty"`
	Model   string                      `json:"model,omitempty"`
	Choices []ChatCompletionChunkChoice `json:"choices,omitempty"`
}

func (c *ChatCompletionChunk) Text() string {
	if c == nil {
		return ""
	}
	var builder strings.Builder
	for _, choice := range c.Choices {
		if choice.Delta.Content != "" {
			builder.WriteString(choice.Delta.Content)
			continue
		}
		if choice.Message.Content != "" {
			builder.WriteString(choice.Message.Content)
		}
	}
	return builder.String()
}

type ResponsesRequest struct {
	Model           string   `json:"model"`
	Input           string   `json:"input"`
	MaxOutputTokens *int     `json:"max_output_tokens,omitempty"`
	Temperature     *float64 `json:"temperature,omitempty"`
	Stream          bool     `json:"stream,omitempty"`
}

type ResponsesContent struct {
	Type string `json:"type,omitempty"`
	Text string `json:"text,omitempty"`
}

type ResponsesOutput struct {
	Type    string             `json:"type,omitempty"`
	Role    string             `json:"role,omitempty"`
	Content []ResponsesContent `json:"content,omitempty"`
}

type ResponsesUsage struct {
	InputTokens  int `json:"input_tokens,omitempty"`
	OutputTokens int `json:"output_tokens,omitempty"`
	TotalTokens  int `json:"total_tokens,omitempty"`
}

type ResponsesResponse struct {
	ID     string            `json:"id,omitempty"`
	Object string            `json:"object,omitempty"`
	Model  string            `json:"model,omitempty"`
	Status string            `json:"status,omitempty"`
	Output []ResponsesOutput `json:"output,omitempty"`
	Usage  ResponsesUsage    `json:"usage,omitempty"`
}

func (r *ResponsesResponse) Text() string {
	if r == nil {
		return ""
	}
	var builder strings.Builder
	for _, output := range r.Output {
		for _, content := range output.Content {
			builder.WriteString(content.Text)
		}
	}
	return builder.String()
}

type ResponsesStreamChunk struct {
	ID     string            `json:"id,omitempty"`
	Object string            `json:"object,omitempty"`
	Model  string            `json:"model,omitempty"`
	Output []ResponsesOutput `json:"output,omitempty"`
}

func (c *ResponsesStreamChunk) Text() string {
	if c == nil {
		return ""
	}
	var builder strings.Builder
	for _, output := range c.Output {
		for _, content := range output.Content {
			builder.WriteString(content.Text)
		}
	}
	return builder.String()
}

type AnthropicMessagesRequest struct {
	Model       string             `json:"model"`
	MaxTokens   int                `json:"max_tokens"`
	Messages    []AnthropicMessage `json:"messages"`
	System      string             `json:"system,omitempty"`
	Temperature *float64           `json:"temperature,omitempty"`
}

type AnthropicMessageContent struct {
	Type string `json:"type,omitempty"`
	Text string `json:"text,omitempty"`
}

type AnthropicMessagesUsage struct {
	InputTokens  int `json:"input_tokens,omitempty"`
	OutputTokens int `json:"output_tokens,omitempty"`
}

type AnthropicMessagesResponse struct {
	ID         string                    `json:"id,omitempty"`
	Type       string                    `json:"type,omitempty"`
	Role       string                    `json:"role,omitempty"`
	Model      string                    `json:"model,omitempty"`
	Content    []AnthropicMessageContent `json:"content,omitempty"`
	StopReason string                    `json:"stop_reason,omitempty"`
	Usage      AnthropicMessagesUsage    `json:"usage,omitempty"`
}

func (r *AnthropicMessagesResponse) Text() string {
	if r == nil {
		return ""
	}
	var builder strings.Builder
	for _, block := range r.Content {
		builder.WriteString(block.Text)
	}
	return builder.String()
}

type ChatCompletionsService struct {
	client *Client
}

func (s *ChatCompletionsService) Create(ctx context.Context, req *ChatCompletionRequest) (*ChatCompletionResponse, error) {
	if req == nil {
		return nil, errorsNew("chat completion request is required")
	}
	var resp ChatCompletionResponse
	if err := s.client.doJSON(ctx, http.MethodPost, "/v1/chat/completions", req, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *ChatCompletionsService) Stream(
	ctx context.Context,
	req *ChatCompletionRequest,
	handler func(*ChatCompletionChunk) error,
) error {
	if req == nil {
		return errorsNew("chat completion request is required")
	}
	if handler == nil {
		return errorsNew("chat completion stream handler is required")
	}

	copyReq := *req
	copyReq.Stream = true

	return s.client.doSSE(ctx, http.MethodPost, "/v1/chat/completions", &copyReq, nil, func(event SSEEvent) error {
		var chunk ChatCompletionChunk
		if err := json.Unmarshal(event.Data, &chunk); err != nil {
			return err
		}
		return handler(&chunk)
	})
}

type ResponsesService struct {
	client *Client
}

func (s *ResponsesService) Create(ctx context.Context, req *ResponsesRequest) (*ResponsesResponse, error) {
	if req == nil {
		return nil, errorsNew("responses request is required")
	}
	var resp ResponsesResponse
	if err := s.client.doJSON(ctx, http.MethodPost, "/v1/responses", req, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *ResponsesService) Stream(
	ctx context.Context,
	req *ResponsesRequest,
	handler func(*ResponsesStreamChunk) error,
) error {
	if req == nil {
		return errorsNew("responses request is required")
	}
	if handler == nil {
		return errorsNew("responses stream handler is required")
	}

	copyReq := *req
	copyReq.Stream = true

	return s.client.doSSE(ctx, http.MethodPost, "/v1/responses", &copyReq, nil, func(event SSEEvent) error {
		var chunk ResponsesStreamChunk
		if err := json.Unmarshal(event.Data, &chunk); err != nil {
			return err
		}
		return handler(&chunk)
	})
}

type MessagesService struct {
	client *Client
}

func (s *MessagesService) Create(
	ctx context.Context,
	req *AnthropicMessagesRequest,
	anthropicVersion string,
) (*AnthropicMessagesResponse, error) {
	if req == nil {
		return nil, errorsNew("anthropic messages request is required")
	}
	if strings.TrimSpace(anthropicVersion) == "" {
		anthropicVersion = DefaultAnthropicVersion
	}

	headers := http.Header{}
	headers.Set("anthropic-version", anthropicVersion)

	var resp AnthropicMessagesResponse
	if err := s.client.doJSON(ctx, http.MethodPost, "/v1/messages", req, headers, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func errorsNew(message string) error {
	return &APIError{Message: message}
}
