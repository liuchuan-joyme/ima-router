package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

public final class VideoKlingText2VideoExample {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();

        var task = client.kling().textToVideo(
            new Requests.KlingTextToVideo(
                "kling-v2-6",
                ExampleSupport.envOr("IMA_KLING_TEXT_PROMPT", "A cinematic camera move through a futuristic showroom with reflective surfaces")
            )
                .mode(ExampleSupport.envOr("IMA_KLING_MODE", "std"))
                .duration(ExampleSupport.envInt("IMA_KLING_DURATION", 5))
                .aspectRatio(ExampleSupport.envOr("IMA_KLING_ASPECT_RATIO", "16:9"))
                .negativePrompt(ExampleSupport.envOr("IMA_KLING_NEGATIVE_PROMPT", ""))
        );

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
