package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

public final class ImageBananaProExample {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();

        var textTask = client.images().generate(
            Requests.GeminiImage.textToImage(
                "gemini-3-pro-image-preview",
                ExampleSupport.envOr("IMA_GEMINI_PRO_PROMPT", "A cinematic fashion editorial portrait, soft light, premium product photography")
            )
                .aspectRatio(ExampleSupport.envOr("IMA_GEMINI_PRO_ASPECT_RATIO", "3:4"))
                .size(ExampleSupport.envOr("IMA_GEMINI_PRO_SIZE", "2K"))
        );

        System.out.println("=== Step 1: text-to-image ===");
        ExampleSupport.printJson("submitted", textTask.raw());

        var textResult = client.images().waitForTask(
            textTask.taskId(),
            ExampleSupport.imageWaitTimeout(),
            ExampleSupport.imagePollInterval(),
            null
        );
        ExampleSupport.printImageOutcome(textResult);

        String sourceUrl = ExampleSupport.imageResultUrl(textResult);
        if (sourceUrl.isBlank()) {
            throw new IllegalStateException("Text-to-image did not return a usable image url");
        }

        var imageTask = client.images().generate(
            Requests.GeminiImage.imageToImage(
                "gemini-3-pro-image-preview",
                ExampleSupport.envOr("IMA_GEMINI_PRO_IMAGE_PROMPT", "Turn this image into a polished luxury campaign poster with richer contrast and refined lighting"),
                java.util.List.of(sourceUrl)
            )
                .aspectRatio(ExampleSupport.envOr("IMA_GEMINI_PRO_IMAGE_ASPECT_RATIO", "3:4"))
                .size(ExampleSupport.envOr("IMA_GEMINI_PRO_IMAGE_SIZE", "2K"))
        );

        System.out.println();
        System.out.println("=== Step 2: image-to-image ===");
        ExampleSupport.printJson("submitted", imageTask.raw());

        var imageResult = client.images().waitForTask(
            imageTask.taskId(),
            ExampleSupport.imageWaitTimeout(),
            ExampleSupport.imagePollInterval(),
            null
        );
        ExampleSupport.printImageOutcome(imageResult);
    }
}
