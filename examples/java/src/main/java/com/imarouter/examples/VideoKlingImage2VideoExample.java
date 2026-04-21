package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

public final class VideoKlingImage2VideoExample {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();

        Requests.KlingImageToVideo request = new Requests.KlingImageToVideo(
            "kling-v2-6",
            ExampleSupport.testImageHeadUrl()
        )
            .prompt(ExampleSupport.envOr("IMA_KLING_IMAGE_PROMPT", "Create a smooth cinematic camera move that brings the reference scene to life"))
            .mode(ExampleSupport.envOr("IMA_KLING_MODE", "std"))
            .duration(ExampleSupport.envInt("IMA_KLING_DURATION", 5))
            .aspectRatio(ExampleSupport.envOr("IMA_KLING_ASPECT_RATIO", "16:9"))
            .negativePrompt(ExampleSupport.envOr("IMA_KLING_NEGATIVE_PROMPT", ""));

        if (ExampleSupport.envBool("IMA_KLING_USE_TAIL", false)) {
            request.imageTail(ExampleSupport.testImageTailUrl());
        }

        var task = client.kling().imageToVideo(request);
        ExampleSupport.printJson("submitted", task.raw());

        var result = client.kling().waitForTask(
            task.taskId(),
            ExampleSupport.videoWaitTimeout(),
            ExampleSupport.videoPollInterval(),
            ExampleSupport::printVideoPollStatus
        );
        ExampleSupport.printVideoOutcome(result);
    }
}
