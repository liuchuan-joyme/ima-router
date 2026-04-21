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
            model="gemini-3-pro-image-preview",
            prompt=os.getenv(
                "IMA_GEMINI_PRO_PROMPT",
                "A cinematic fashion editorial portrait, soft light, premium product photography",
            ),
            aspect_ratio=os.getenv("IMA_GEMINI_PRO_ASPECT_RATIO", "3:4"),
            size=os.getenv("IMA_GEMINI_PRO_SIZE", "2K"),
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
            model="gemini-3-pro-image-preview",
            prompt=os.getenv(
                "IMA_GEMINI_PRO_IMAGE_PROMPT",
                "Turn this image into a polished luxury campaign poster with richer contrast and refined lighting",
            ),
            images=source_url,
            aspect_ratio=os.getenv("IMA_GEMINI_PRO_IMAGE_ASPECT_RATIO", "3:4"),
            size=os.getenv("IMA_GEMINI_PRO_IMAGE_SIZE", "2K"),
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
