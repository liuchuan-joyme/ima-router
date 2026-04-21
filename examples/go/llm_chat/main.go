package main

import (
	"context"
	"fmt"

	exampleutil "github.com/liuchuan-joyme/ima-router/examples/go/internal/exampleutil"
	imarouter "github.com/liuchuan-joyme/ima-router/sdk/go"
)

func main() {
	client, err := exampleutil.MakeClient()
	if err != nil {
		panic(err)
	}

	resp, err := client.ChatCompletions.Create(context.Background(), &imarouter.ChatCompletionRequest{
		Model: exampleutil.EnvOr("IMA_CHAT_MODEL", "gpt-5.3-codex"),
		Messages: []imarouter.ChatMessage{
			{Role: "system", Content: "You are a concise assistant."},
			{Role: "user", Content: "Introduce ima-router in 3 short bullet points."},
		},
	})
	if err != nil {
		panic(err)
	}

	fmt.Println(resp.Text())
}
