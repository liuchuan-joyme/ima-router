package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

public final class ImageBanana2Example {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();

        var textTask = client.images().generate(
            Requests.GeminiImage.textToImage(
                "gemini-3.1-flash-image-preview",
                ExampleSupport.envOr("IMA_GEMINI_FLASH_PROMPT", "A playful app onboarding illustration, clean geometry, vibrant colors")
            )
                .aspectRatio(ExampleSupport.envOr("IMA_GEMINI_FLASH_ASPECT_RATIO", "1:1"))
                .size(ExampleSupport.envOr("IMA_GEMINI_FLASH_SIZE", "1K"))
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
                "gemini-3.1-flash-image-preview",
                ExampleSupport.envOr("IMA_GEMINI_FLASH_IMAGE_PROMPT", "Remix this image into a brighter, friendlier launch visual with stronger shape language"),
                java.util.List.of(sourceUrl)
            )
                .aspectRatio(ExampleSupport.envOr("IMA_GEMINI_FLASH_IMAGE_ASPECT_RATIO", "1:1"))
                .size(ExampleSupport.envOr("IMA_GEMINI_FLASH_IMAGE_SIZE", "1K"))
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
