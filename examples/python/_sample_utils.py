from __future__ import annotations

import json
import os
from typing import Any

from _bootstrap import bootstrap_local_sdk

bootstrap_local_sdk()

from ima_router import IMARouter


DEFAULT_TEST_IMAGE_URL = "https://file.fashionlabs.cn/doc_image/r2v_tea_pic1.jpg"
DEFAULT_TEST_IMAGE_HEAD_URL = (
    "https://dev-jiman.oss-cn-hangzhou.aliyuncs.com/jm/20260316/"
    "ac5fe9ec6640426bafd6200b254b4b5f.png"
)
DEFAULT_TEST_IMAGE_TAIL_URL = (
    "https://dev-jiman.oss-cn-hangzhou.aliyuncs.com/jm/20260316/"
    "321175f98af24025b835debeb18002cc.png"
)


def make_client() -> IMARouter:
    timeout_seconds = float(os.getenv("IMA_HTTP_TIMEOUT_SECONDS", "300"))
    return IMARouter(
        api_key=os.getenv("IMA_API_KEY"),
        base_url=os.getenv("IMA_BASE_URL", "https://api.imarouter.com"),
        timeout=timeout_seconds,
    )


def test_image_url() -> str:
    return os.getenv("IMA_TEST_IMAGE_URL", DEFAULT_TEST_IMAGE_URL)


def test_image_head_url() -> str:
    return os.getenv(
        "IMA_TEST_IMAGE_HEAD_URL",
        os.getenv("IMA_TEST_IMAGE_URL", DEFAULT_TEST_IMAGE_HEAD_URL),
    )


def test_image_tail_url() -> str:
    return os.getenv("IMA_TEST_IMAGE_TAIL_URL", DEFAULT_TEST_IMAGE_TAIL_URL)


def print_json(title: str, payload: Any) -> None:
    print(title)
    print(json.dumps(payload, indent=2, ensure_ascii=False))


def print_image_outcome(result: dict[str, Any]) -> None:
    data = result.get("data") or {}
    print(f"status={data.get('status')}")
    if data.get("url"):
        print(f"url={data['url']}")
    if data.get("error"):
        print_json("error", data["error"])


def print_video_outcome(result: dict[str, Any]) -> None:
    task = video_task_view(result)
    print(f"status={task.get('status')}")
    results = task.get("results") or []
    if results:
        for index, item in enumerate(results, start=1):
            print(f"result[{index}]={item.get('url')}")
    metadata = task.get("metadata") or {}
    if isinstance(metadata, dict) and metadata.get("url"):
        print(f"metadata.url={metadata['url']}")
    if task.get("error"):
        print_json("error", task["error"])


def print_midjourney_outcome(result: dict[str, Any]) -> None:
    print(f"status={result.get('status')}")
    if result.get("imageUrl"):
        print(f"imageUrl={result['imageUrl']}")
    urls = result.get("urls") or []
    for index, url in enumerate(urls, start=1):
        print(f"urls[{index}]={url}")
    if result.get("failReason"):
        print(f"failReason={result['failReason']}")


def image_result_url(result: dict[str, Any]) -> str | None:
    data = result.get("data") or {}
    url = data.get("url")
    return url if isinstance(url, str) and url else None


def midjourney_result_url(result: dict[str, Any]) -> str | None:
    urls = result.get("urls") or []
    if isinstance(urls, list):
        for url in urls:
            if isinstance(url, str) and url:
                return url

    image_url = result.get("imageUrl")
    return image_url if isinstance(image_url, str) and image_url else None


def print_video_poll_status(result: dict[str, Any], attempt: int) -> None:
    task = video_task_view(result)
    status = task.get("status")
    progress = task.get("progress")
    print(f"[poll {attempt}] status={status} progress={progress}", flush=True)


def video_task_view(result: dict[str, Any]) -> dict[str, Any]:
    status = result.get("status")
    progress = result.get("progress")
    if status is not None or progress is not None:
        return result

    data = result.get("data")
    if isinstance(data, dict):
        return data

    return result
