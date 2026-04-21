from __future__ import annotations

import os

from _sample_utils import make_client, print_json, print_video_outcome, print_video_poll_status
from ima_router import SeedanceVideoRequest


def main() -> None:
    client = make_client()
    try:
        wait_timeout_seconds = float(os.getenv("IMA_VIDEO_WAIT_TIMEOUT_SECONDS", "3600"))
        poll_interval_seconds = float(os.getenv("IMA_VIDEO_POLL_INTERVAL_SECONDS", "10"))

        request = SeedanceVideoRequest.text_to_video(
            model="seedance-2.0",
            prompt=os.getenv(
                "IMA_SEEDANCE_TEXT_PROMPT",
                "A cinematic sunrise over a modern city skyline, smooth drone motion, warm golden light",
            ),
            duration=int(os.getenv("IMA_SEEDANCE_DURATION", "5")),
            aspect_ratio=os.getenv("IMA_SEEDANCE_ASPECT_RATIO", "16:9"),
            resolution=os.getenv("IMA_SEEDANCE_RESOLUTION", "720p"),
            audio=os.getenv("IMA_SEEDANCE_AUDIO", "false").lower() == "true",
        )

        task = client.videos.create(request)
        print_json("submitted", task)

        result = client.videos.wait(
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
