from __future__ import annotations

import os

from _bootstrap import bootstrap_local_sdk

bootstrap_local_sdk()

from ima_router import IMARouter


def main() -> None:
    client = IMARouter(
        api_key=os.getenv("IMA_API_KEY"),
        base_url=os.getenv("IMA_BASE_URL", "https://api.imarouter.com"),
    )

    with client.chat.completions.create(
        model=os.getenv("IMA_STREAM_MODEL", "gpt-5.4"),
        messages=[
            {"role": "user", "content": "Write a short launch tagline for ima-router."},
        ],
        stream=True,
    ) as stream:
        for text in stream.iter_text():
            print(text, end="", flush=True)

    print()
    client.close()


if __name__ == "__main__":
    main()
