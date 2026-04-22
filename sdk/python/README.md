# ima-router Python SDK

Python SDK for the IMA Router LLM and multimodal endpoints defined in the current `openapi.yaml`.

## What This SDK Covers

- LLM: `POST /v1/chat/completions`, `POST /v1/responses`, `POST /v1/messages`
- Images: `POST /v1/images/generations`, `GET /v1/images/generations/{task_id}`
- Videos: `POST /v1/videos`, `GET /v1/videos/{task_id}`
- Kling: `POST /kling/v1/videos/text2video`, `POST /kling/v1/videos/image2video`
- Midjourney: `POST /mj/submit/imagine`, `GET /mj/task/{task_id}/fetch`

The platform supports **many more models than the few used in this README**.
The snippets below are only representative examples for how to call the SDK.

For the latest supported models, parameters, and capability updates, always check:

- [Official website](https://www.imarouter.com/)

Features:

- OpenAI-compatible chat completions and responses
- Anthropic-compatible messages
- Gemini image task submission and polling
- Seedance video task submission and polling
- Kling text-to-video and image-to-video helpers
- Midjourney imagine/blend/change helpers
- Bearer auth with `sk-...`
- Streaming support for chat and responses

## Install

```bash
pip install -e sdk/python
```

## Quick Example

Representative chat example:

```python
from ima_router import IMARouter

client = IMARouter(api_key="YOUR_API_KEY")

response = client.chat.completions.create(
    model="gpt-4o",
    messages=[{"role": "user", "content": "Say hello in one sentence."}],
)

print(response["choices"][0]["message"]["content"])
```

## Multimodal Example

Representative image example:

```python
from ima_router import IMARouter, GeminiImageRequest

client = IMARouter(api_key="YOUR_API_KEY")

task = client.images.generate(
    GeminiImageRequest.text_to_image(
        model="gemini-3-pro-image-preview",
        prompt="A cinematic product hero shot",
        aspect_ratio="16:9",
        size="2K",
    )
)

result = client.images.wait(task["task_id"])
print(result)
```

## Base URL

Default base URL:

```text
https://api.imarouter.com
```

You can also override it:

```python
client = IMARouter(
    api_key="YOUR_API_KEY",
    base_url="https://open-route.fashionlabs.cn",
)
```

## Notes

- Do not treat the model names in this README as the full supported model list.
- New models can be added before this README is updated.
- Server-side validation and the official website are the source of truth for model-specific parameters and current model coverage.
