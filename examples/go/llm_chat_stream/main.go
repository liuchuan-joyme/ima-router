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

	err = client.ChatCompletions.Stream(context.Background(), &imarouter.ChatCompletionRequest{
		Model: exampleutil.EnvOr("IMA_STREAM_MODEL", "gpt-5.4"),
		Messages: []imarouter.ChatMessage{
			{Role: "user", Content: "Write a short launch tagline for ima-router."},
		},
	}, func(chunk *imarouter.ChatCompletionChunk) error {
		fmt.Print(chunk.Text())
		return nil
	})
	if err != nil {
		panic(err)
	}

	fmt.Println()
}
