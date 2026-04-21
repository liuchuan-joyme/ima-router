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

	maxOutputTokens := 256
	resp, err := client.Responses.Create(context.Background(), &imarouter.ResponsesRequest{
		Model:           exampleutil.EnvOr("IMA_RESPONSES_MODEL", "gpt-5.3-codex"),
		Input:           "Write a one paragraph Go doc comment for an ima-router client.",
		MaxOutputTokens: &maxOutputTokens,
	})
	if err != nil {
		panic(err)
	}

	fmt.Println(resp.Text())
}
