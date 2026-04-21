from __future__ import annotations

import os

from _sample_utils import (
    make_client,
    print_json,
    print_video_poll_status,
    print_video_outcome,
    test_image_head_url,
    test_image_tail_url,
)
from ima_router import KlingImageToVideoRequest


def main() -> None:
    client = make_client()
    try:
        wait_timeout_seconds = float(os.getenv("IMA_VIDEO_WAIT_TIMEOUT_SECONDS", "3600"))
        poll_interval_seconds = float(os.getenv("IMA_VIDEO_POLL_INTERVAL_SECONDS", "10"))

        use_tail = os.getenv("IMA_KLING_USE_TAIL", "false").lower() == "true"
        tail_image = test_image_tail_url() if use_tail else None

        request = KlingImageToVideoRequest(
            model="kling-v2-6",
            image=test_image_head_url(),
            image_tail=tail_image,
            prompt=os.getenv(
                "IMA_KLING_IMAGE_PROMPT",
                "Create a smooth cinematic camera move that brings the reference scene to life",
            ),
            mode=os.getenv("IMA_KLING_MODE", "std"),
            duration=int(os.getenv("IMA_KLING_DURATION", "5")),
            aspect_ratio=os.getenv("IMA_KLING_ASPECT_RATIO", "16:9"),
            negative_prompt=os.getenv("IMA_KLING_NEGATIVE_PROMPT"),
        )

        task = client.kling.image_to_video(request)
        print_json("submitted", task)

        result = client.kling.wait(
            task["task_id"],
            timeout_seconds=wait_timeout_seconds,
            interval_seconds=poll_interval_seconds,
            on_poll=print_video_poll_status,
        )
        print_video_outcome(result)
    finally:
        client.close()


if __name__ == "__main__":
    main()
