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

    response = client.messages.create(
        model=os.getenv("IMA_ANTHROPIC_MODEL", "claude-sonnet-4-6"),
        max_tokens=256,
        system="You are a helpful assistant for ima-router users.",
        messages=[
            {"role": "user", "content": "Explain when I should use the messages endpoint."},
        ],
    )

    texts: list[str] = []
    for block in response.get("content", []):
        text = block.get("text")
        if text:
            texts.append(text)

    print("\n".join(texts))
    client.close()


if __name__ == "__main__":
    main()
