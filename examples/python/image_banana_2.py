from __future__ import annotations

import os

from _sample_utils import image_result_url, make_client, print_image_outcome, print_json
from ima_router import GeminiImageRequest


def main() -> None:
    client = make_client()
    try:
        wait_timeout_seconds = float(os.getenv("IMA_IMAGE_WAIT_TIMEOUT_SECONDS", "1800"))
        poll_interval_seconds = float(os.getenv("IMA_IMAGE_POLL_INTERVAL_SECONDS", "8"))

        text_to_image_request = GeminiImageRequest.text_to_image(
            model="gemini-3.1-flash-image-preview",
            prompt=os.getenv(
                "IMA_GEMINI_FLASH_PROMPT",
                "A playful app onboarding illustration, clean geometry, vibrant colors",
            ),
            aspect_ratio=os.getenv("IMA_GEMINI_FLASH_ASPECT_RATIO", "1:1"),
            size=os.getenv("IMA_GEMINI_FLASH_SIZE", "1K"),
        )

        print("=== Step 1: text-to-image ===")
        text_task = client.images.generate(text_to_image_request)
        print_json("submitted", text_task)

        text_result = client.images.wait(
            text_task["task_id"],
            timeout_seconds=wait_timeout_seconds,
            interval_seconds=poll_interval_seconds,
        )
        print_image_outcome(text_result)

        source_url = image_result_url(text_result)
        if not source_url:
            raise RuntimeError("Text-to-image did not return a usable result URL for image-to-image.")

        image_to_image_request = GeminiImageRequest.image_to_image(
            model="gemini-3.1-flash-image-preview",
            prompt=os.getenv(
                "IMA_GEMINI_FLASH_IMAGE_PROMPT",
                "Remix this image into a brighter, friendlier launch visual with stronger shape language",
            ),
            images=source_url,
            aspect_ratio=os.getenv("IMA_GEMINI_FLASH_IMAGE_ASPECT_RATIO", "1:1"),
            size=os.getenv("IMA_GEMINI_FLASH_IMAGE_SIZE", "1K"),
        )

        print()
        print("=== Step 2: image-to-image ===")
        image_task = client.images.generate(image_to_image_request)
        print_json("submitted", image_task)

        image_result = client.images.wait(
            image_task["task_id"],
            timeout_seconds=wait_timeout_seconds,
            interval_seconds=poll_interval_seconds,
        )
        print_image_outcome(image_result)
    finally:
        client.close()


if __name__ == "__main__":
    main()
