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
		"gemini-3.1-flash-image-preview",
		exampleutil.EnvOr("IMA_GEMINI_FLASH_PROMPT", "A playful app onboarding illustration, clean geometry, vibrant colors"),
		exampleutil.EnvOr("IMA_GEMINI_FLASH_ASPECT_RATIO", "1:1"),
		exampleutil.EnvOr("IMA_GEMINI_FLASH_SIZE", "1K"),
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
		"gemini-3.1-flash-image-preview",
		exampleutil.EnvOr("IMA_GEMINI_FLASH_IMAGE_PROMPT", "Remix this image into a brighter, friendlier launch visual with stronger shape language"),
		[]string{sourceURL},
		exampleutil.EnvOr("IMA_GEMINI_FLASH_IMAGE_ASPECT_RATIO", "1:1"),
		exampleutil.EnvOr("IMA_GEMINI_FLASH_IMAGE_SIZE", "1K"),
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
