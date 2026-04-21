package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

public final class VideoSD2Text2VideoExample {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();

        var task = client.videos().create(
            Requests.SeedanceVideo.textToVideo(
                "seedance-2.0",
                ExampleSupport.envOr("IMA_SEEDANCE_TEXT_PROMPT", "A cinematic sunrise over a modern city skyline, smooth drone motion, warm golden light")
            )
                .duration(ExampleSupport.envInt("IMA_SEEDANCE_DURATION", 5))
                .metadata(
                    new Requests.SeedanceVideoMetadata()
                        .aspectRatio(ExampleSupport.envOr("IMA_SEEDANCE_ASPECT_RATIO", "16:9"))
                        .resolution(ExampleSupport.envOr("IMA_SEEDANCE_RESOLUTION", "720p"))
                        .audio(ExampleSupport.envBool("IMA_SEEDANCE_AUDIO", false))
                )
        );

        ExampleSupport.printJson("submitted", task.raw());

        var result = client.videos().waitForTask(
            task.taskId(),
            ExampleSupport.videoWaitTimeout(),
            ExampleSupport.videoPollInterval(),
            ExampleSupport::printVideoPollStatus
        );
        ExampleSupport.printVideoOutcome(result);
    }
}
