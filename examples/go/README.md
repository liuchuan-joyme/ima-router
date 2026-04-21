# Go Examples

These examples import the local SDK from `sdk/go` and automatically load `.env` from the repository root.

## Run

```bash
go run ./examples/go/llm_chat
go run ./examples/go/llm_chat_stream
go run ./examples/go/llm_responses
go run ./examples/go/llm_messages
go run ./examples/go/llm_chat_matrix

go run ./examples/go/image_banana_pro
go run ./examples/go/image_banana_2
go run ./examples/go/image_midjourney_niji7

go run ./examples/go/video_sd2_text2video
go run ./examples/go/video_sd2_image2video
go run ./examples/go/video_kling_text2video
go run ./examples/go/video_kling_image2video
```

Key environment variables are the same as the Python examples:

- `IMA_API_KEY`
- `IMA_BASE_URL`
- `IMA_TEST_IMAGE_URL`
- `IMA_TEST_IMAGE_HEAD_URL`
- `IMA_TEST_IMAGE_TAIL_URL`
- `IMA_HTTP_TIMEOUT_SECONDS`
- `IMA_IMAGE_WAIT_TIMEOUT_SECONDS`
- `IMA_VIDEO_WAIT_TIMEOUT_SECONDS`
