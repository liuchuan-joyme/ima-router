from __future__ import annotations

import json
import os
import time
from typing import Any, Callable, Iterable, Mapping

import httpx

from ._stream import IMARouterStream
from .multimodal import (
    GeminiImageRequest,
    KlingImageToVideoRequest,
    KlingTextToVideoRequest,
    MidjourneyBlendRequest,
    MidjourneyChangeRequest,
    MidjourneyImagineRequest,
    SeedanceVideoRequest,
)


class IMARouterError(RuntimeError):
    pass


class APIError(IMARouterError):
    def __init__(self, status_code: int, message: str, *, body: Any = None) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.body = body

    @classmethod
    def from_response(cls, response: httpx.Response) -> "APIError":
        body: Any
        message = f"IMA Router request failed with status {response.status_code}."

        try:
            body = response.json()
        except ValueError:
            body = response.text

        if isinstance(body, dict):
            error = body.get("error")
            if isinstance(error, str) and error:
                message = error
            elif isinstance(error, dict) and error:
                message = (
                    error.get("message")
                    or error.get("type")
                    or json.dumps(error, ensure_ascii=False)
                )
            else:
                maybe_message = body.get("message")
                if isinstance(maybe_message, str) and maybe_message:
                    message = maybe_message
        elif isinstance(body, str) and body.strip():
            message = body.strip()

        return cls(response.status_code, message, body=body)


def _task_view(result: dict[str, Any]) -> dict[str, Any]:
    status = result.get("status")
    progress = result.get("progress")
    if status is not None or progress is not None:
        return result

    data = result.get("data")
    if isinstance(data, dict):
        return data

    return result


class _BaseResource:
    def __init__(self, client: "IMARouter") -> None:
        self._client = client

    def _payload(
        self,
        request: Any = None,
        **kwargs: Any,
    ) -> dict[str, Any]:
        if request is not None and kwargs:
            raise ValueError("Pass either a request object or keyword arguments, not both.")

        if request is None:
            return {key: value for key, value in kwargs.items() if value is not None}

        if hasattr(request, "to_payload"):
            return request.to_payload()

        if isinstance(request, Mapping):
            return {key: value for key, value in request.items() if value is not None}

        raise TypeError("request must be a mapping or an object with to_payload().")

    def _wait_for_task(
        self,
        *,
        task_id: str,
        fetch: Callable[[str], dict[str, Any]],
        is_terminal: Callable[[dict[str, Any]], bool],
        timeout_seconds: float,
        interval_seconds: float,
        on_poll: Callable[[dict[str, Any], int], None] | None = None,
    ) -> dict[str, Any]:
        deadline = time.monotonic() + timeout_seconds
        attempt = 0

        while True:
            result = fetch(task_id)
            attempt += 1
            if on_poll is not None:
                on_poll(result, attempt)
            if is_terminal(result):
                return result
            if time.monotonic() >= deadline:
                raise TimeoutError(f"Timed out while waiting for task {task_id}.")
            time.sleep(interval_seconds)


class ChatCompletionsAPI(_BaseResource):
    def create(
        self,
        *,
        model: str,
        messages: Iterable[Mapping[str, Any]],
        stream: bool = False,
        temperature: float | None = None,
        max_tokens: int | None = None,
        tools: Iterable[Mapping[str, Any]] | None = None,
        tool_choice: str | Mapping[str, Any] | None = None,
        **extra_body: Any,
    ) -> dict[str, Any] | IMARouterStream:
        payload: dict[str, Any] = {
            "model": model,
            "messages": list(messages),
        }
        if stream:
            payload["stream"] = True
        if temperature is not None:
            payload["temperature"] = temperature
        if max_tokens is not None:
            payload["max_tokens"] = max_tokens
        if tools is not None:
            payload["tools"] = list(tools)
        if tool_choice is not None:
            payload["tool_choice"] = tool_choice
        payload.update(extra_body)

        return self._client._post("/v1/chat/completions", payload, stream=stream)


class ChatNamespace:
    def __init__(self, client: "IMARouter") -> None:
        self.completions = ChatCompletionsAPI(client)


class ResponsesAPI(_BaseResource):
    def create(
        self,
        *,
        model: str,
        input: str,
        max_output_tokens: int | None = None,
        temperature: float | None = None,
        stream: bool = False,
        **extra_body: Any,
    ) -> dict[str, Any] | IMARouterStream:
        payload: dict[str, Any] = {
            "model": model,
            "input": input,
        }
        if max_output_tokens is not None:
            payload["max_output_tokens"] = max_output_tokens
        if temperature is not None:
            payload["temperature"] = temperature
        if stream:
            payload["stream"] = True
        payload.update(extra_body)

        return self._client._post("/v1/responses", payload, stream=stream)


class MessagesAPI(_BaseResource):
    def create(
        self,
        *,
        model: str,
        max_tokens: int,
        messages: Iterable[Mapping[str, Any]],
        system: str | None = None,
        temperature: float | None = None,
        anthropic_version: str = "2023-06-01",
        **extra_body: Any,
    ) -> dict[str, Any]:
        payload: dict[str, Any] = {
            "model": model,
            "max_tokens": max_tokens,
            "messages": list(messages),
        }
        if system is not None:
            payload["system"] = system
        if temperature is not None:
            payload["temperature"] = temperature
        payload.update(extra_body)

        return self._client._post(
            "/v1/messages",
            payload,
            headers={"anthropic-version": anthropic_version},
        )


class ImagesAPI(_BaseResource):
    def generate(
        self,
        request: GeminiImageRequest | Mapping[str, Any] | None = None,
        **kwargs: Any,
    ) -> dict[str, Any]:
        payload = self._payload(request, **kwargs)
        return self._client._post("/v1/images/generations", payload)

    create = generate

    def get(self, task_id: str) -> dict[str, Any]:
        return self._client._get(f"/v1/images/generations/{task_id}")

    def wait(
        self,
        task_id: str,
        *,
        timeout_seconds: float = 1800.0,
        interval_seconds: float = 5.0,
        on_poll: Callable[[dict[str, Any], int], None] | None = None,
    ) -> dict[str, Any]:
        return self._wait_for_task(
            task_id=task_id,
            fetch=self.get,
            timeout_seconds=timeout_seconds,
            interval_seconds=interval_seconds,
            on_poll=on_poll,
            is_terminal=lambda result: (result.get("data") or {}).get("status")
            in {"succeeded", "failed"},
        )


class VideosAPI(_BaseResource):
    def create(
        self,
        request: SeedanceVideoRequest | Mapping[str, Any] | None = None,
        **kwargs: Any,
    ) -> dict[str, Any]:
        payload = self._payload(request, **kwargs)
        return self._client._post("/v1/videos", payload)

    def text_to_video(
        self,
        *,
        model: str,
        prompt: str,
        duration: int = 5,
        aspect_ratio: str | None = "16:9",
        resolution: str | None = "720p",
        audio: bool | str | None = False,
    ) -> dict[str, Any]:
        request = SeedanceVideoRequest.text_to_video(
            model=model,
            prompt=prompt,
            duration=duration,
            aspect_ratio=aspect_ratio,
            resolution=resolution,
            audio=audio,
        )
        return self.create(request)

    def image_to_video(
        self,
        *,
        model: str,
        images: str | list[str],
        prompt: str | None = None,
        duration: int = 5,
        aspect_ratio: str | None = "16:9",
        resolution: str | None = "720p",
        audio: bool | str | None = False,
        role_mode: str = "reference",
    ) -> dict[str, Any]:
        request = SeedanceVideoRequest.image_to_video(
            model=model,
            images=images,
            prompt=prompt,
            duration=duration,
            aspect_ratio=aspect_ratio,
            resolution=resolution,
            audio=audio,
            role_mode=role_mode,
        )
        return self.create(request)

    def get(self, task_id: str) -> dict[str, Any]:
        return self._client._get(f"/v1/videos/{task_id}")

    def wait(
        self,
        task_id: str,
        *,
        timeout_seconds: float = 1800.0,
        interval_seconds: float = 8.0,
        on_poll: Callable[[dict[str, Any], int], None] | None = None,
    ) -> dict[str, Any]:
        return self._wait_for_task(
            task_id=task_id,
            fetch=self.get,
            timeout_seconds=timeout_seconds,
            interval_seconds=interval_seconds,
            on_poll=on_poll,
            is_terminal=lambda result: _task_view(result).get("status") in {"completed", "failed"},
        )


class KlingAPI(_BaseResource):
    def text_to_video(
        self,
        request: KlingTextToVideoRequest | Mapping[str, Any] | None = None,
        **kwargs: Any,
    ) -> dict[str, Any]:
        payload = self._payload(request, **kwargs)
        return self._client._post("/kling/v1/videos/text2video", payload)

    def image_to_video(
        self,
        request: KlingImageToVideoRequest | Mapping[str, Any] | None = None,
        **kwargs: Any,
    ) -> dict[str, Any]:
        payload = self._payload(request, **kwargs)
        return self._client._post("/kling/v1/videos/image2video", payload)

    def get(self, task_id: str) -> dict[str, Any]:
        return self._client._get(f"/v1/videos/{task_id}")

    def wait(
        self,
        task_id: str,
        *,
        timeout_seconds: float = 1800.0,
        interval_seconds: float = 8.0,
        on_poll: Callable[[dict[str, Any], int], None] | None = None,
    ) -> dict[str, Any]:
        return self._wait_for_task(
            task_id=task_id,
            fetch=self.get,
            timeout_seconds=timeout_seconds,
            interval_seconds=interval_seconds,
            on_poll=on_poll,
            is_terminal=lambda result: _task_view(result).get("status") in {"completed", "failed"},
        )


class MidjourneyAPI(_BaseResource):
    def imagine(
        self,
        request: MidjourneyImagineRequest | Mapping[str, Any] | None = None,
        *,
        x_youchuan_setting: str | None = None,
        **kwargs: Any,
    ) -> dict[str, Any]:
        payload = self._payload(request, **kwargs)
        headers = (
            {"x-youchuan-setting": x_youchuan_setting}
            if x_youchuan_setting is not None
            else None
        )
        return self._client._post("/mj/submit/imagine", payload, headers=headers)

    def blend(
        self,
        request: MidjourneyBlendRequest | Mapping[str, Any] | None = None,
        **kwargs: Any,
    ) -> dict[str, Any]:
        payload = self._payload(request, **kwargs)
        return self._client._post("/mj/submit/blend", payload)

    def change(
        self,
        request: MidjourneyChangeRequest | Mapping[str, Any] | None = None,
        **kwargs: Any,
    ) -> dict[str, Any]:
        payload = self._payload(request, **kwargs)
        return self._client._post("/mj/submit/change", payload)

    def get(self, task_id: str) -> dict[str, Any]:
        return self._client._get(f"/mj/task/{task_id}/fetch")

    def wait(
        self,
        task_id: str,
        *,
        timeout_seconds: float = 1800.0,
        interval_seconds: float = 8.0,
        on_poll: Callable[[dict[str, Any], int], None] | None = None,
    ) -> dict[str, Any]:
        return self._wait_for_task(
            task_id=task_id,
            fetch=self.get,
            timeout_seconds=timeout_seconds,
            interval_seconds=interval_seconds,
            on_poll=on_poll,
            is_terminal=lambda result: result.get("status") in {"SUCCESS", "FAILURE"},
        )

    def fetch_image(self, task_id: str) -> bytes:
        return self._client._get_binary(f"/mj/image/{task_id}")


class IMARouter:
    def __init__(
        self,
        *,
        api_key: str | None = None,
        base_url: str | None = None,
        timeout: float = 60.0,
        default_headers: Mapping[str, str] | None = None,
    ) -> None:
        api_key = api_key or os.getenv("IMA_API_KEY")
        if not api_key:
            raise ValueError("api_key is required. You can also set IMA_API_KEY.")

        self._base_url = (base_url or os.getenv("IMA_BASE_URL") or "https://api.imarouter.com").rstrip("/")
        headers = {
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
        }
        if default_headers:
            headers.update(default_headers)

        self._http = httpx.Client(
            timeout=timeout,
            headers=headers,
            follow_redirects=True,
        )

        self.chat = ChatNamespace(self)
        self.responses = ResponsesAPI(self)
        self.messages = MessagesAPI(self)
        self.images = ImagesAPI(self)
        self.videos = VideosAPI(self)
        self.kling = KlingAPI(self)
        self.midjourney = MidjourneyAPI(self)

    def close(self) -> None:
        self._http.close()

    def __enter__(self) -> "IMARouter":
        return self

    def __exit__(self, exc_type: object, exc: object, tb: object) -> None:
        self.close()

    def _build_url(self, path: str) -> str:
        normalized_path = path if path.startswith("/") else f"/{path}"
        if self._base_url.endswith("/v1") and normalized_path.startswith("/v1/"):
            normalized_path = normalized_path[3:]
        return f"{self._base_url}{normalized_path}"

    def _post(
        self,
        path: str,
        payload: Mapping[str, Any],
        *,
        headers: Mapping[str, str] | None = None,
        stream: bool = False,
    ) -> dict[str, Any] | IMARouterStream:
        url = self._build_url(path)

        try:
            if stream:
                request = self._http.build_request("POST", url, json=payload, headers=headers)
                response = self._http.send(request, stream=True)
                if response.is_error:
                    try:
                        response.read()
                    finally:
                        response.close()
                    raise APIError.from_response(response)
                return IMARouterStream(response)

            response = self._http.post(url, json=payload, headers=headers)
        except httpx.HTTPError as exc:
            raise IMARouterError(str(exc)) from exc

        if response.is_error:
            raise APIError.from_response(response)

        if not response.content:
            return {}

        return response.json()

    def _get(
        self,
        path: str,
        *,
        headers: Mapping[str, str] | None = None,
    ) -> dict[str, Any]:
        url = self._build_url(path)

        try:
            response = self._http.get(url, headers=headers)
        except httpx.HTTPError as exc:
            raise IMARouterError(str(exc)) from exc

        if response.is_error:
            raise APIError.from_response(response)

        if not response.content:
            return {}

        return response.json()

    def _get_binary(
        self,
        path: str,
        *,
        headers: Mapping[str, str] | None = None,
    ) -> bytes:
        url = self._build_url(path)

        try:
            response = self._http.get(url, headers=headers)
        except httpx.HTTPError as exc:
            raise IMARouterError(str(exc)) from exc

        if response.is_error:
            raise APIError.from_response(response)

        return response.content
