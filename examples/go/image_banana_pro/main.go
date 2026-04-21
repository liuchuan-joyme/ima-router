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

	ctx := context.Background()
	textReq, err := imarouter.NewGeminiTextToImageRequest(
		"gemini-3-pro-image-preview",
		exampleutil.EnvOr("IMA_GEMINI_PRO_PROMPT", "A cinematic fashion editorial portrait, soft light, premium product photography"),
		exampleutil.EnvOr("IMA_GEMINI_PRO_ASPECT_RATIO", "3:4"),
		exampleutil.EnvOr("IMA_GEMINI_PRO_SIZE", "2K"),
	)
	if err != nil {
		panic(err)
	}

	fmt.Println("=== Step 1: text-to-image ===")
	textTask, err := client.Images.Generate(ctx, textReq)
	if err != nil {
		panic(err)
	}
	exampleutil.PrintJSON("submitted", textTask)

	textResult, err := client.Images.Wait(ctx, textTask.TaskID, exampleutil.ImageWaitTimeout(), exampleutil.ImagePollInterval(), nil)
	if err != nil {
		panic(err)
	}
	exampleutil.PrintImageOutcome(textResult)

	sourceURL := exampleutil.ImageResultURL(textResult)
	if sourceURL == "" {
		panic("text-to-image did not return a usable image url")
	}

	imageReq, err := imarouter.NewGeminiImageToImageRequest(
		"gemini-3-pro-image-preview",
		exampleutil.EnvOr("IMA_GEMINI_PRO_IMAGE_PROMPT", "Turn this image into a polished luxury campaign poster with richer contrast and refined lighting"),
		[]string{sourceURL},
		exampleutil.EnvOr("IMA_GEMINI_PRO_IMAGE_ASPECT_RATIO", "3:4"),
		exampleutil.EnvOr("IMA_GEMINI_PRO_IMAGE_SIZE", "2K"),
	)
	if err != nil {
		panic(err)
	}

	fmt.Println()
	fmt.Println("=== Step 2: image-to-image ===")
	imageTask, err := client.Images.Generate(ctx, imageReq)
	if err != nil {
		panic(err)
	}
	exampleutil.PrintJSON("submitted", imageTask)

	imageResult, err := client.Images.Wait(ctx, imageTask.TaskID, exampleutil.ImageWaitTimeout(), exampleutil.ImagePollInterval(), nil)
	if err != nil {
		panic(err)
	}
	exampleutil.PrintImageOutcome(imageResult)
}
