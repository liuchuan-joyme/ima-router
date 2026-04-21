from __future__ import annotations

import os

from _bootstrap import bootstrap_local_sdk

bootstrap_local_sdk()

from ima_router import IMARouter


def main() -> None:
    model_names = [
        item.strip()
        for item in os.getenv(
            "IMA_CHAT_MODELS",
            "gpt-5.3-codex,gpt-5.4,claude-sonnet-4-6,claude-opus-4-6",
        ).split(",")
        if item.strip()
    ]

    client = IMARouter(
        api_key=os.getenv("IMA_API_KEY"),
        base_url=os.getenv("IMA_BASE_URL", "https://api.imarouter.com"),
    )

    try:
        for model in model_names:
            print(f"=== {model} ===")
            try:
                response = client.chat.completions.create(
                    model=model,
                    messages=[
                        {
                            "role": "user",
                            "content": f"Reply with exactly: OK {model}",
                        }
                    ],
                    temperature=0,
                    max_tokens=120,
                )
                print(response["choices"][0]["message"]["content"])
            except Exception as exc:
                print(f"ERROR: {exc}")
            print()
    finally:
        client.close()


if __name__ == "__main__":
    main()
