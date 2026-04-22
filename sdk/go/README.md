# ima-router Go SDK

Go SDK for the current IMA Router LLM and multimodal endpoints.

## What This SDK Covers

- LLM: `/v1/chat/completions`, `/v1/responses`, `/v1/messages`
- Images: `/v1/images/generations`
- Videos: `/v1/videos`
- Kling: `/kling/v1/videos/text2video`, `/kling/v1/videos/image2video`
- Midjourney: `/mj/submit/imagine`, `/mj/task/{task_id}/fetch`

The platform supports **many more models than the few shown in this README**.
These examples are only representative request patterns.

For the latest supported models, parameters, and capability updates, always check:

- [Official API docs](https://open-route-api.fashionlabs.cn/431672322e0)

The SDK uses the Go standard library only.

## Quick Example

Representative chat example:

```go
package main

import (
    "context"
    "fmt"

    imarouter "github.com/liuchuan-joyme/ima-router/sdk/go"
)

func main() {
    client, err := imarouter.NewClient("YOUR_API_KEY")
    if err != nil {
        panic(err)
    }

    resp, err := client.ChatCompletions.Create(context.Background(), &imarouter.ChatCompletionRequest{
        Model: "gpt-4o",
        Messages: []imarouter.ChatMessage{
            {Role: "user", Content: "Say hello in one sentence."},
        },
    })
    if err != nil {
        panic(err)
    }

    fmt.Println(resp.Text())
}
```

## Notes

- Do not treat the model name in this README as the complete supported model matrix.
- New models may be available before this README is refreshed.
- Model-specific parameters should follow the official documentation and server-side validation.
