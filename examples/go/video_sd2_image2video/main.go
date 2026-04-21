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
	req, err := imarouter.NewSeedanceImageToVideoRequest(
		"seedance-2.0",
		exampleutil.TestImageHeadURL(),
		exampleutil.EnvOr("IMA_SEEDANCE_IMAGE_PROMPT", "Transform the reference image into a cinematic product reveal with gentle camera movement"),
		exampleutil.EnvInt("IMA_SEEDANCE_DURATION", 5),
		exampleutil.EnvOr("IMA_SEEDANCE_ASPECT_RATIO", "16:9"),
		exampleutil.EnvOr("IMA_SEEDANCE_RESOLUTION", "720p"),
		exampleutil.EnvBool("IMA_SEEDANCE_AUDIO", false),
		exampleutil.EnvOr("IMA_SEEDANCE_ROLE_MODE", "reference"),
	)
	if err != nil {
		panic(err)
	}

	task, err := client.Videos.Create(ctx, req)
	if err != nil {
		panic(err)
	}
	exampleutil.PrintJSON("submitted", task)

	result, err := client.Videos.Wait(ctx, task.View().TaskID, exampleutil.VideoWaitTimeout(), exampleutil.VideoPollInterval(), exampleutil.PrintVideoPollStatus)
	if err != nil {
		panic(err)
	}
	exampleutil.PrintVideoOutcome(result)
}
