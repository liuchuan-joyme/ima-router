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

	resp, err := client.Messages.Create(context.Background(), &imarouter.AnthropicMessagesRequest{
		Model:     exampleutil.EnvOr("IMA_ANTHROPIC_MODEL", "claude-sonnet-4-6"),
		MaxTokens: 256,
		System:    "You are a helpful assistant for ima-router users.",
		Messages: []imarouter.AnthropicMessage{
			{Role: "user", Content: "Explain when I should use the messages endpoint."},
		},
	}, imarouter.DefaultAnthropicVersion)
	if err != nil {
		panic(err)
	}

	fmt.Println(resp.Text())
}
