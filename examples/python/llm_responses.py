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

    response = client.responses.create(
        model=os.getenv("IMA_RESPONSES_MODEL", "gpt-5.3-codex"),
        input="Write a one paragraph Python docstring for an ima-router client.",
        max_output_tokens=256,
    )

    texts: list[str] = []
    for item in response.get("output", []):
        for content in item.get("content", []):
            text = content.get("text")
            if text:
                texts.append(text)

    print("\n".join(texts))
    client.close()


if __name__ == "__main__":
    main()
