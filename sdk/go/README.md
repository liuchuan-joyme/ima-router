# ima-router Go SDK

Go SDK for the current IMA Router LLM and multimodal endpoints.

Supported endpoint groups:

- LLM: `/v1/chat/completions`, `/v1/responses`, `/v1/messages`
- Images: `/v1/images/generations`
- Videos: `/v1/videos`
- Kling: `/kling/v1/videos/text2video`, `/kling/v1/videos/image2video`
- Midjourney: `/mj/submit/imagine`, `/mj/task/{task_id}/fetch`

The SDK uses the Go standard library only.

## Quick Example

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
