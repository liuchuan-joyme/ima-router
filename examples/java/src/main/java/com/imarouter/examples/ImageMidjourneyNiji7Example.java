package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

public final class ImageMidjourneyNiji7Example {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();

        var textTask = client.midjourney().imagine(
            Requests.MidjourneyImagine.niji(
                ExampleSupport.envOr("IMA_MJ_NIJI_PROMPT", "anime key visual of a futuristic courier girl running through neon rain"),
                7,
                ExampleSupport.envOr("IMA_MJ_ASPECT_RATIO", "9:16"),
                ExampleSupport.envOr("IMA_MJ_EXTRA_ARGS", "")
            )
        );

        System.out.println("=== Step 1: text-to-image ===");
        ExampleSupport.printJson("submitted", textTask.raw());

        var textResult = client.midjourney().waitForTask(
            textTask.taskId(),
            ExampleSupport.imageWaitTimeout(),
            ExampleSupport.imagePollInterval(),
            null
        );
        ExampleSupport.printMidjourneyOutcome(textResult);

        String sourceUrl = ExampleSupport.midjourneyResultUrl(textResult);
        if (sourceUrl.isBlank()) {
            throw new IllegalStateException("Midjourney text-to-image did not return a usable image url");
        }

        var imageRequest = Requests.MidjourneyImagine.nijiWithImage(
            sourceUrl,
            ExampleSupport.envOr("IMA_MJ_NIJI_IMAGE_PROMPT", "Remix this image into a stronger anime poster composition with richer atmosphere and motion"),
            7,
            ExampleSupport.envOr("IMA_MJ_IMAGE_ASPECT_RATIO", ExampleSupport.envOr("IMA_MJ_ASPECT_RATIO", "9:16")),
            ExampleSupport.envOr("IMA_MJ_IMAGE_EXTRA_ARGS", ExampleSupport.envOr("IMA_MJ_EXTRA_ARGS", ""))
        );

        var imageTask = client.midjourney().imagine(imageRequest);
        System.out.println();
        System.out.println("=== Step 2: image-to-image ===");
        ExampleSupport.printJson("submitted", imageTask.raw());

        var imageResult = client.midjourney().waitForTask(
            imageTask.taskId(),
            ExampleSupport.imageWaitTimeout(),
            ExampleSupport.imagePollInterval(),
            null
        );
        ExampleSupport.printMidjourneyOutcome(imageResult);
    }
}
