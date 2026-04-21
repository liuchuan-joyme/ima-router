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
	textReq := imarouter.NewMidjourneyNijiRequest(
		exampleutil.EnvOr("IMA_MJ_NIJI_PROMPT", "anime key visual of a futuristic courier girl running through neon rain"),
		7,
		exampleutil.EnvOr("IMA_MJ_ASPECT_RATIO", "9:16"),
		exampleutil.EnvOr("IMA_MJ_EXTRA_ARGS", ""),
	)

	fmt.Println("=== Step 1: text-to-image ===")
	textTask, err := client.Midjourney.Imagine(ctx, textReq, "")
	if err != nil {
		panic(err)
	}
	exampleutil.PrintJSON("submitted", textTask)

	textResult, err := client.Midjourney.Wait(ctx, textTask.Result, exampleutil.ImageWaitTimeout(), exampleutil.ImagePollInterval(), nil)
	if err != nil {
		panic(err)
	}
	exampleutil.PrintMidjourneyOutcome(textResult)

	sourceURL := exampleutil.MidjourneyResultURL(textResult)
	if sourceURL == "" {
		panic("midjourney text-to-image did not return a usable image url")
	}

	imageReq, err := imarouter.NewMidjourneyNijiWithImageRequest(
		sourceURL,
		exampleutil.EnvOr("IMA_MJ_NIJI_IMAGE_PROMPT", "Remix this image into a stronger anime poster composition with richer atmosphere and motion"),
		7,
		exampleutil.EnvOr("IMA_MJ_IMAGE_ASPECT_RATIO", exampleutil.EnvOr("IMA_MJ_ASPECT_RATIO", "9:16")),
		exampleutil.EnvOr("IMA_MJ_IMAGE_EXTRA_ARGS", exampleutil.EnvOr("IMA_MJ_EXTRA_ARGS", "")),
	)
	if err != nil {
		panic(err)
	}

	fmt.Println()
	fmt.Println("=== Step 2: image-to-image ===")
	imageTask, err := client.Midjourney.Imagine(ctx, imageReq, "")
	if err != nil {
		panic(err)
	}
	exampleutil.PrintJSON("submitted", imageTask)

	imageResult, err := client.Midjourney.Wait(ctx, imageTask.Result, exampleutil.ImageWaitTimeout(), exampleutil.ImagePollInterval(), nil)
	if err != nil {
		panic(err)
	}
	exampleutil.PrintMidjourneyOutcome(imageResult)
}
