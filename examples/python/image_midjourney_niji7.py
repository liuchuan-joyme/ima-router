from __future__ import annotations

import os

from _sample_utils import make_client, midjourney_result_url, print_json, print_midjourney_outcome
from ima_router import MidjourneyImagineRequest


def main() -> None:
    client = make_client()
    try:
        wait_timeout_seconds = float(os.getenv("IMA_IMAGE_WAIT_TIMEOUT_SECONDS", "1800"))
        poll_interval_seconds = float(os.getenv("IMA_IMAGE_POLL_INTERVAL_SECONDS", "8"))

        text_to_image_request = MidjourneyImagineRequest.niji(
            os.getenv(
                "IMA_MJ_NIJI_PROMPT",
                "anime key visual of a futuristic courier girl running through neon rain",
            ),
            version=7,
            aspect_ratio=os.getenv("IMA_MJ_ASPECT_RATIO", "9:16"),
            extra_args=os.getenv("IMA_MJ_EXTRA_ARGS"),
        )

        print("=== Step 1: text-to-image ===")
        text_task = client.midjourney.imagine(text_to_image_request)
        print_json("submitted", text_task)

        text_result = client.midjourney.wait(
            text_task["result"],
            timeout_seconds=wait_timeout_seconds,
            interval_seconds=poll_interval_seconds,
        )
        print_midjourney_outcome(text_result)

        source_url = midjourney_result_url(text_result)
        if not source_url:
            raise RuntimeError("Midjourney text-to-image did not return a usable result URL.")

        image_to_image_request = MidjourneyImagineRequest.niji_with_image(
            source_url,
            os.getenv(
                "IMA_MJ_NIJI_IMAGE_PROMPT",
                "Remix this image into a stronger anime poster composition with richer atmosphere and motion",
            ),
            version=7,
            aspect_ratio=os.getenv("IMA_MJ_IMAGE_ASPECT_RATIO", os.getenv("IMA_MJ_ASPECT_RATIO", "9:16")),
            extra_args=os.getenv("IMA_MJ_IMAGE_EXTRA_ARGS", os.getenv("IMA_MJ_EXTRA_ARGS")),
        )

        print()
        print("=== Step 2: image-to-image ===")
        image_task = client.midjourney.imagine(image_to_image_request)
        print_json("submitted", image_task)

        image_result = client.midjourney.wait(
            image_task["result"],
            timeout_seconds=wait_timeout_seconds,
            interval_seconds=poll_interval_seconds,
        )
        print_midjourney_outcome(image_result)
    finally:
        client.close()


if __name__ == "__main__":
    main()
