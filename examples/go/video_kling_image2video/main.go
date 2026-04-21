package main

import (
	"context"

	exampleutil "github.com/liuchuan-joyme/ima-router/examples/go/internal/exampleutil"
	imarouter "github.com/liuchuan-joyme/ima-router/sdk/go"
)

func main() {
	client, err := exampleutil.MakeClient()
	if err != nil {
		panic(err)
	}

	ctx := context.Background()
	tailImage := ""
	if exampleutil.EnvBool("IMA_KLING_USE_TAIL", false) {
		tailImage = exampleutil.TestImageTailURL()
	}

	req := &imarouter.KlingImageToVideoRequest{
		Model:          "kling-v2-6",
		Image:          exampleutil.TestImageHeadURL(),
		ImageTail:      tailImage,
		Prompt:         exampleutil.EnvOr("IMA_KLING_IMAGE_PROMPT", "Create a smooth cinematic camera move that brings the reference scene to life"),
		Mode:           exampleutil.EnvOr("IMA_KLING_MODE", "std"),
		Duration:       exampleutil.EnvInt("IMA_KLING_DURATION", 5),
		AspectRatio:    exampleutil.EnvOr("IMA_KLING_ASPECT_RATIO", "16:9"),
		NegativePrompt: exampleutil.EnvOr("IMA_KLING_NEGATIVE_PROMPT", ""),
	}

	task, err := client.Kling.ImageToVideo(ctx, req)
	if err != nil {
		panic(err)
	}
	exampleutil.PrintJSON("submitted", task)

	result, err := client.Kling.Wait(ctx, task.View().TaskID, exampleutil.VideoWaitTimeout(), exampleutil.VideoPollInterval(), exampleutil.PrintVideoPollStatus)
	if err != nil {
		panic(err)
	}
	exampleutil.PrintVideoOutcome(result)
}
