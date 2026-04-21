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

    response = client.chat.completions.create(
        model=os.getenv("IMA_CHAT_MODEL", "gpt-4o"),
        messages=[
            {"role": "system", "content": "You are a concise assistant."},
            {"role": "user", "content": "Introduce ima-router in 3 short bullet points."},
        ],
        temperature=0.7,
    )

    print(response["choices"][0]["message"]["content"])
    client.close()


if __name__ == "__main__":
    main()
