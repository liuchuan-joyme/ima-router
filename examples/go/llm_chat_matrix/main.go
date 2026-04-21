package main

import (
	"context"
	"fmt"
	"strings"

	exampleutil "github.com/liuchuan-joyme/ima-router/examples/go/internal/exampleutil"
	imarouter "github.com/liuchuan-joyme/ima-router/sdk/go"
)

func main() {
	client, err := exampleutil.MakeClient()
	if err != nil {
		panic(err)
	}

	models := strings.Split(exampleutil.EnvOr("IMA_CHAT_MODELS", "gpt-5.3-codex,gpt-5.4,claude-sonnet-4-6,claude-opus-4-6"), ",")
	for _, model := range models {
		model = strings.TrimSpace(model)
		if model == "" {
			continue
		}

		fmt.Printf("=== %s ===\n", model)
		maxTokens := 120
		temperature := 0.0
		resp, err := client.ChatCompletions.Create(context.Background(), &imarouter.ChatCompletionRequest{
			Model: model,
			Messages: []imarouter.ChatMessage{
				{Role: "user", Content: "Reply with exactly: OK " + model},
			},
			MaxTokens:   &maxTokens,
			Temperature: &temperature,
		})
		if err != nil {
			fmt.Printf("ERROR: %v\n\n", err)
			continue
		}

		fmt.Println(resp.Text())
		fmt.Println()
	}
}
