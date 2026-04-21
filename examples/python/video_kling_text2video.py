from __future__ import annotations

import os

from _sample_utils import make_client, print_json, print_video_outcome, print_video_poll_status
from ima_router import KlingTextToVideoRequest


def main() -> None:
    client = make_client()
    try:
        wait_timeout_seconds = float(os.getenv("IMA_VIDEO_WAIT_TIMEOUT_SECONDS", "3600"))
        poll_interval_seconds = float(os.getenv("IMA_VIDEO_POLL_INTERVAL_SECONDS", "10"))

        request = KlingTextToVideoRequest(
            model="kling-v2-6",
            prompt=os.getenv(
                "IMA_KLING_TEXT_PROMPT",
                "A cinematic camera move through a futuristic showroom with reflective surfaces",
            ),
            mode=os.getenv("IMA_KLING_MODE", "std"),
            duration=int(os.getenv("IMA_KLING_DURATION", "5")),
            aspect_ratio=os.getenv("IMA_KLING_ASPECT_RATIO", "16:9"),
            negative_prompt=os.getenv("IMA_KLING_NEGATIVE_PROMPT"),
        )

        task = client.kling.text_to_video(request)
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
