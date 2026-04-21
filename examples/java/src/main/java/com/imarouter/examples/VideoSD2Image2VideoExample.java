package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

public final class VideoSD2Image2VideoExample {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();

        var task = client.videos().create(
            Requests.SeedanceVideo.imageToVideo(
                "seedance-2.0",
                ExampleSupport.testImageHeadUrl()
            )
                .prompt(ExampleSupport.envOr("IMA_SEEDANCE_IMAGE_PROMPT", "Transform the reference image into a cinematic product reveal with gentle camera movement"))
                .duration(ExampleSupport.envInt("IMA_SEEDANCE_DURATION", 5))
                .metadata(
                    new Requests.SeedanceVideoMetadata()
                        .aspectRatio(ExampleSupport.envOr("IMA_SEEDANCE_ASPECT_RATIO", "16:9"))
                        .resolution(ExampleSupport.envOr("IMA_SEEDANCE_RESOLUTION", "720p"))
                        .audio(ExampleSupport.envBool("IMA_SEEDANCE_AUDIO", false))
                        .roleMode(ExampleSupport.envOr("IMA_SEEDANCE_ROLE_MODE", "reference"))
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
