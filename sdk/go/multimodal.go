package imarouter

import (
	"context"
	"fmt"
	"net/http"
	"strings"
	"time"
)

type GeminiImageRequest struct {
	Model       string   `json:"model"`
	Prompt      string   `json:"prompt"`
	AspectRatio string   `json:"aspect_ratio,omitempty"`
	Size        string   `json:"size,omitempty"`
	Image       string   `json:"image,omitempty"`
	Images      []string `json:"images,omitempty"`
}

func NewGeminiTextToImageRequest(model string, prompt string, aspectRatio string, size string) (*GeminiImageRequest, error) {
	req := &GeminiImageRequest{
		Model:       model,
		Prompt:      prompt,
		AspectRatio: aspectRatio,
		Size:        size,
	}
	return req, req.Validate()
}

func NewGeminiImageToImageRequest(model string, prompt string, images []string, aspectRatio string, size string) (*GeminiImageRequest, error) {
	req := &GeminiImageRequest{
		Model:       model,
		Prompt:      prompt,
		AspectRatio: aspectRatio,
		Size:        size,
	}
	if len(images) == 1 {
		req.Image = images[0]
	} else {
		req.Images = append(req.Images, images...)
	}
	return req, req.Validate()
}

func (r *GeminiImageRequest) Validate() error {
	if r == nil {
		return fmt.Errorf("gemini image request is required")
	}
	if strings.TrimSpace(r.Model) == "" {
		return fmt.Errorf("model is required")
	}
	if strings.TrimSpace(r.Prompt) == "" {
		return fmt.Errorf("prompt is required")
	}
	if r.Image != "" && len(r.Images) > 0 {
		return fmt.Errorf("use either image or images, not both")
	}

	return nil
}

type ImageUsage struct {
	InputTokens  int `json:"input_tokens,omitempty"`
	OutputTokens int `json:"output_tokens,omitempty"`
	TotalTokens  int `json:"total_tokens,omitempty"`
}

type ImageTaskError struct {
	Message string `json:"message,omitempty"`
}

type ImageTaskSubmission struct {
	ID        string  `json:"id,omitempty"`
	TaskID    string  `json:"task_id,omitempty"`
	Object    string  `json:"object,omitempty"`
	Model     string  `json:"model,omitempty"`
	Status    string  `json:"status,omitempty"`
	Progress  int     `json:"progress,omitempty"`
	AmountUSD float64 `json:"amount_usd,omitempty"`
	CreatedAt int64   `json:"created_at,omitempty"`
}

type ImageTaskData struct {
	TaskID   string          `json:"task_id,omitempty"`
	Status   string          `json:"status,omitempty"`
	Format   string          `json:"format,omitempty"`
	URL      string          `json:"url,omitempty"`
	Error    *ImageTaskError `json:"error,omitempty"`
	Metadata any             `json:"metadata,omitempty"`
	Usage    ImageUsage      `json:"usage,omitempty"`
}

func (d *ImageTaskData) PrimaryURL() string {
	if d == nil {
		return ""
	}
	return d.URL
}

type ImageTaskResponse struct {
	Code string        `json:"code,omitempty"`
	Data ImageTaskData `json:"data"`
}

type SeedanceVideoMetadata struct {
	AspectRatio        string           `json:"aspect_ratio,omitempty"`
	Resolution         string           `json:"resolution,omitempty"`
	Audio              any              `json:"audio,omitempty"`
	RoleMode           string           `json:"role_mode,omitempty"`
	ReferenceVideoURLs []string         `json:"reference_video_urls,omitempty"`
	ReferenceAudioURLs []string         `json:"reference_audio_urls,omitempty"`
	MCPList            []map[string]any `json:"mcp_list,omitempty"`
}

type SeedanceVideoRequest struct {
	Model          string                 `json:"model"`
	Prompt         string                 `json:"prompt,omitempty"`
	Images         []string               `json:"images,omitempty"`
	InputReference string                 `json:"input_reference,omitempty"`
	Duration       int                    `json:"duration,omitempty"`
	Seconds        string                 `json:"seconds,omitempty"`
	Size           string                 `json:"size,omitempty"`
	Metadata       *SeedanceVideoMetadata `json:"metadata,omitempty"`
}

func NewSeedanceTextToVideoRequest(
	model string,
	prompt string,
	duration int,
	aspectRatio string,
	resolution string,
	audio bool,
) (*SeedanceVideoRequest, error) {
	req := &SeedanceVideoRequest{
		Model:    model,
		Prompt:   prompt,
		Duration: duration,
		Metadata: &SeedanceVideoMetadata{
			AspectRatio: aspectRatio,
			Resolution:  resolution,
			Audio:       audio,
		},
	}
	return req, req.Validate()
}

func NewSeedanceImageToVideoRequest(
	model string,
	imageURL string,
	prompt string,
	duration int,
	aspectRatio string,
	resolution string,
	audio bool,
	roleMode string,
) (*SeedanceVideoRequest, error) {
	req := &SeedanceVideoRequest{
		Model:    model,
		Prompt:   prompt,
		Images:   []string{imageURL},
		Duration: duration,
		Metadata: &SeedanceVideoMetadata{
			AspectRatio: aspectRatio,
			Resolution:  resolution,
			Audio:       audio,
			RoleMode:    roleMode,
		},
	}
	return req, req.Validate()
}

func (r *SeedanceVideoRequest) Validate() error {
	if r == nil {
		return fmt.Errorf("seedance video request is required")
	}
	if strings.TrimSpace(r.Model) == "" {
		return fmt.Errorf("model is required")
	}
	if strings.TrimSpace(r.Prompt) == "" && len(r.Images) == 0 && r.Metadata == nil {
		return fmt.Errorf("seedance video request needs prompt, images, or metadata references")
	}

	return nil
}

type VideoResult struct {
	URL         string `json:"url,omitempty"`
	Width       int    `json:"width,omitempty"`
	Height      int    `json:"height,omitempty"`
	Duration    int    `json:"duration,omitempty"`
	Size        int64  `json:"size,omitempty"`
	ContentType string `json:"content_type,omitempty"`
}

type VideoMetadata struct {
	URL string `json:"url,omitempty"`
}

type VideoTaskError struct {
	Code    string `json:"code,omitempty"`
	Message string `json:"message,omitempty"`
}

type VideoUsage struct {
	PromptTokens     int `json:"prompt_tokens,omitempty"`
	CompletionTokens int `json:"completion_tokens,omitempty"`
	TotalTokens      int `json:"total_tokens,omitempty"`
}

type VideoTask struct {
	ID          string          `json:"id,omitempty"`
	TaskID      string          `json:"task_id,omitempty"`
	Object      string          `json:"object,omitempty"`
	Model       string          `json:"model,omitempty"`
	Status      string          `json:"status,omitempty"`
	Progress    int             `json:"progress,omitempty"`
	AmountUSD   float64         `json:"amount_usd,omitempty"`
	CreatedAt   int64           `json:"created_at,omitempty"`
	CompletedAt int64           `json:"completed_at,omitempty"`
	Metadata    *VideoMetadata  `json:"metadata,omitempty"`
	Results     []VideoResult   `json:"results,omitempty"`
	Usage       *VideoUsage     `json:"usage,omitempty"`
	Error       *VideoTaskError `json:"error,omitempty"`
}

func (t *VideoTask) PrimaryURL() string {
	if t == nil {
		return ""
	}
	if len(t.Results) > 0 && t.Results[0].URL != "" {
		return t.Results[0].URL
	}
	if t.Metadata != nil {
		return t.Metadata.URL
	}
	return ""
}

type VideoTaskEnvelope struct {
	VideoTask
	Code string     `json:"code,omitempty"`
	Data *VideoTask `json:"data,omitempty"`
}

func (e *VideoTaskEnvelope) View() *VideoTask {
	if e == nil {
		return nil
	}
	if e.Data != nil {
		return e.Data
	}
	return &e.VideoTask
}

type KlingTextToVideoRequest struct {
	Model          string         `json:"model"`
	Prompt         string         `json:"prompt"`
	NegativePrompt string         `json:"negative_prompt,omitempty"`
	Mode           string         `json:"mode,omitempty"`
	Duration       int            `json:"duration,omitempty"`
	AspectRatio    string         `json:"aspect_ratio,omitempty"`
	CFGScale       *float64       `json:"cfg_scale,omitempty"`
	CameraControl  map[string]any `json:"camera_control,omitempty"`
}

func (r *KlingTextToVideoRequest) Validate() error {
	if r == nil {
		return fmt.Errorf("kling text-to-video request is required")
	}
	if strings.TrimSpace(r.Model) == "" {
		return fmt.Errorf("model is required")
	}
	if strings.TrimSpace(r.Prompt) == "" {
		return fmt.Errorf("prompt is required")
	}
	return nil
}

type KlingImageToVideoRequest struct {
	Model          string         `json:"model"`
	Image          string         `json:"image"`
	ImageTail      string         `json:"image_tail,omitempty"`
	Prompt         string         `json:"prompt,omitempty"`
	NegativePrompt string         `json:"negative_prompt,omitempty"`
	Mode           string         `json:"mode,omitempty"`
	Duration       int            `json:"duration,omitempty"`
	AspectRatio    string         `json:"aspect_ratio,omitempty"`
	CFGScale       *float64       `json:"cfg_scale,omitempty"`
	CameraControl  map[string]any `json:"camera_control,omitempty"`
}

func (r *KlingImageToVideoRequest) Validate() error {
	if r == nil {
		return fmt.Errorf("kling image-to-video request is required")
	}
	if strings.TrimSpace(r.Model) == "" {
		return fmt.Errorf("model is required")
	}
	if strings.TrimSpace(r.Image) == "" {
		return fmt.Errorf("image is required")
	}
	return nil
}

type MidjourneyImagineRequest struct {
	Prompt string `json:"prompt"`
}

func NewMidjourneyNijiRequest(prompt string, version int, aspectRatio string, extraArgs string) *MidjourneyImagineRequest {
	if version == 0 {
		version = 7
	}

	parts := []string{strings.TrimSpace(prompt), fmt.Sprintf("--niji %d", version)}
	if aspectRatio != "" {
		parts = append(parts, "--ar "+aspectRatio)
	}
	if strings.TrimSpace(extraArgs) != "" {
		parts = append(parts, strings.TrimSpace(extraArgs))
	}

	return &MidjourneyImagineRequest{Prompt: strings.Join(parts, " ")}
}

func NewMidjourneyNijiWithImageRequest(
	imageURL string,
	prompt string,
	version int,
	aspectRatio string,
	extraArgs string,
) (*MidjourneyImagineRequest, error) {
	if strings.TrimSpace(imageURL) == "" {
		return nil, fmt.Errorf("image url is required")
	}
	if version == 0 {
		version = 7
	}

	parts := []string{strings.TrimSpace(imageURL), strings.TrimSpace(prompt), fmt.Sprintf("--niji %d", version)}
	if aspectRatio != "" {
		parts = append(parts, "--ar "+aspectRatio)
	}
	if strings.TrimSpace(extraArgs) != "" {
		parts = append(parts, strings.TrimSpace(extraArgs))
	}

	return &MidjourneyImagineRequest{Prompt: strings.Join(parts, " ")}, nil
}

type MidjourneyBlendRequest struct {
	Base64Array []string `json:"base64Array"`
	Prompt      string   `json:"prompt,omitempty"`
}

type MidjourneyChangeRequest struct {
	Action     string `json:"action"`
	TaskID     string `json:"taskId"`
	Index      int    `json:"index,omitempty"`
	Prompt     string `json:"prompt,omitempty"`
	MaskBase64 string `json:"maskBase64,omitempty"`
	CustomID   string `json:"customId,omitempty"`
}

type MidjourneySubmitResponse struct {
	Code        int    `json:"code"`
	Result      string `json:"result"`
	Description string `json:"description,omitempty"`
}

type MidjourneyTaskResponse struct {
	MJID       string   `json:"mj_id,omitempty"`
	Status     string   `json:"status,omitempty"`
	Progress   string   `json:"progress,omitempty"`
	ImageURL   string   `json:"imageUrl,omitempty"`
	URLs       []string `json:"urls,omitempty"`
	Action     string   `json:"action,omitempty"`
	FailReason string   `json:"failReason,omitempty"`
}

func (r *MidjourneyTaskResponse) PrimaryURL() string {
	if r == nil {
		return ""
	}
	for _, url := range r.URLs {
		if url != "" {
			return url
		}
	}
	return r.ImageURL
}

type ImagesService struct {
	client *Client
}

func (s *ImagesService) Generate(ctx context.Context, req *GeminiImageRequest) (*ImageTaskSubmission, error) {
	if req == nil {
		return nil, fmt.Errorf("image generation request is required")
	}
	if err := req.Validate(); err != nil {
		return nil, err
	}

	var resp ImageTaskSubmission
	if err := s.client.doJSON(ctx, http.MethodPost, "/v1/images/generations", req, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *ImagesService) Get(ctx context.Context, taskID string) (*ImageTaskResponse, error) {
	var resp ImageTaskResponse
	if err := s.client.doJSON(ctx, http.MethodGet, "/v1/images/generations/"+taskID, nil, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *ImagesService) Wait(
	ctx context.Context,
	taskID string,
	timeout time.Duration,
	interval time.Duration,
	onPoll func(*ImageTaskResponse, int),
) (*ImageTaskResponse, error) {
	return waitForTask(ctx, taskID, timeout, interval, s.Get, func(resp *ImageTaskResponse) bool {
		if resp == nil {
			return false
		}
		return resp.Data.Status == "succeeded" || resp.Data.Status == "failed"
	}, onPoll)
}

type VideosService struct {
	client *Client
}

func (s *VideosService) Create(ctx context.Context, req *SeedanceVideoRequest) (*VideoTaskEnvelope, error) {
	if req == nil {
		return nil, fmt.Errorf("video request is required")
	}
	if err := req.Validate(); err != nil {
		return nil, err
	}

	var resp VideoTaskEnvelope
	if err := s.client.doJSON(ctx, http.MethodPost, "/v1/videos", req, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *VideosService) Get(ctx context.Context, taskID string) (*VideoTaskEnvelope, error) {
	var resp VideoTaskEnvelope
	if err := s.client.doJSON(ctx, http.MethodGet, "/v1/videos/"+taskID, nil, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *VideosService) Wait(
	ctx context.Context,
	taskID string,
	timeout time.Duration,
	interval time.Duration,
	onPoll func(*VideoTaskEnvelope, int),
) (*VideoTaskEnvelope, error) {
	return waitForTask(ctx, taskID, timeout, interval, s.Get, func(resp *VideoTaskEnvelope) bool {
		if resp == nil || resp.View() == nil {
			return false
		}
		status := resp.View().Status
		return status == "completed" || status == "failed"
	}, onPoll)
}

type KlingService struct {
	client *Client
}

func (s *KlingService) TextToVideo(ctx context.Context, req *KlingTextToVideoRequest) (*VideoTaskEnvelope, error) {
	if req == nil {
		return nil, fmt.Errorf("kling text-to-video request is required")
	}
	if err := req.Validate(); err != nil {
		return nil, err
	}

	var resp VideoTaskEnvelope
	if err := s.client.doJSON(ctx, http.MethodPost, "/kling/v1/videos/text2video", req, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *KlingService) ImageToVideo(ctx context.Context, req *KlingImageToVideoRequest) (*VideoTaskEnvelope, error) {
	if req == nil {
		return nil, fmt.Errorf("kling image-to-video request is required")
	}
	if err := req.Validate(); err != nil {
		return nil, err
	}

	var resp VideoTaskEnvelope
	if err := s.client.doJSON(ctx, http.MethodPost, "/kling/v1/videos/image2video", req, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *KlingService) Get(ctx context.Context, taskID string) (*VideoTaskEnvelope, error) {
	var resp VideoTaskEnvelope
	if err := s.client.doJSON(ctx, http.MethodGet, "/v1/videos/"+taskID, nil, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *KlingService) Wait(
	ctx context.Context,
	taskID string,
	timeout time.Duration,
	interval time.Duration,
	onPoll func(*VideoTaskEnvelope, int),
) (*VideoTaskEnvelope, error) {
	return waitForTask(ctx, taskID, timeout, interval, s.Get, func(resp *VideoTaskEnvelope) bool {
		if resp == nil || resp.View() == nil {
			return false
		}
		status := resp.View().Status
		return status == "completed" || status == "failed"
	}, onPoll)
}

type MidjourneyService struct {
	client *Client
}

func (s *MidjourneyService) Imagine(
	ctx context.Context,
	req *MidjourneyImagineRequest,
	xYouchuanSetting string,
) (*MidjourneySubmitResponse, error) {
	if req == nil || strings.TrimSpace(req.Prompt) == "" {
		return nil, fmt.Errorf("midjourney imagine request is required")
	}

	headers := http.Header{}
	if strings.TrimSpace(xYouchuanSetting) != "" {
		headers.Set("x-youchuan-setting", xYouchuanSetting)
	}

	var resp MidjourneySubmitResponse
	if err := s.client.doJSON(ctx, http.MethodPost, "/mj/submit/imagine", req, headers, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *MidjourneyService) Blend(ctx context.Context, req *MidjourneyBlendRequest) (*MidjourneySubmitResponse, error) {
	if req == nil || len(req.Base64Array) < 2 || len(req.Base64Array) > 5 {
		return nil, fmt.Errorf("midjourney blend requires between 2 and 5 images")
	}

	var resp MidjourneySubmitResponse
	if err := s.client.doJSON(ctx, http.MethodPost, "/mj/submit/blend", req, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *MidjourneyService) Change(ctx context.Context, req *MidjourneyChangeRequest) (*MidjourneySubmitResponse, error) {
	if req == nil {
		return nil, fmt.Errorf("midjourney change request is required")
	}

	var resp MidjourneySubmitResponse
	if err := s.client.doJSON(ctx, http.MethodPost, "/mj/submit/change", req, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *MidjourneyService) Get(ctx context.Context, taskID string) (*MidjourneyTaskResponse, error) {
	var resp MidjourneyTaskResponse
	if err := s.client.doJSON(ctx, http.MethodGet, "/mj/task/"+taskID+"/fetch", nil, nil, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

func (s *MidjourneyService) Wait(
	ctx context.Context,
	taskID string,
	timeout time.Duration,
	interval time.Duration,
	onPoll func(*MidjourneyTaskResponse, int),
) (*MidjourneyTaskResponse, error) {
	return waitForTask(ctx, taskID, timeout, interval, s.Get, func(resp *MidjourneyTaskResponse) bool {
		if resp == nil {
			return false
		}
		return resp.Status == "SUCCESS" || resp.Status == "FAILURE"
	}, onPoll)
}

func (s *MidjourneyService) FetchImage(ctx context.Context, taskID string) ([]byte, error) {
	return s.client.doBinary(ctx, http.MethodGet, "/mj/image/"+taskID, nil, nil)
}
