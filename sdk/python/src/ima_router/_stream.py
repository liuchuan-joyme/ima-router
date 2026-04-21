from __future__ import annotations

import json
from dataclasses import dataclass
from typing import Any, Iterator

import httpx


@dataclass(frozen=True)
class StreamEvent:
    event: str | None
    data: Any


class IMARouterStream:
    def __init__(self, response: httpx.Response) -> None:
        self._response = response
        self._closed = False
        self._consumed = False

    def __enter__(self) -> "IMARouterStream":
        return self

    def __exit__(self, exc_type: object, exc: object, tb: object) -> None:
        self.close()

    def __iter__(self) -> Iterator[StreamEvent]:
        return self.iter_events()

    def close(self) -> None:
        if not self._closed:
            self._response.close()
            self._closed = True

    def iter_events(self) -> Iterator[StreamEvent]:
        if self._consumed:
            raise RuntimeError("Stream has already been consumed.")

        self._consumed = True
        event_name: str | None = None
        data_lines: list[str] = []

        try:
            for line in self._response.iter_lines():
                if not line:
                    if data_lines:
                        payload = "\n".join(data_lines)
                        data_lines.clear()
                        if payload == "[DONE]":
                            return
                        yield StreamEvent(event=event_name, data=_decode_event_data(payload))
                    event_name = None
                    continue

                if line.startswith(":"):
                    continue
                if line.startswith("event:"):
                    event_name = line[6:].strip() or None
                    continue
                if line.startswith("data:"):
                    data_lines.append(line[5:].lstrip())

            if data_lines:
                payload = "\n".join(data_lines)
                if payload != "[DONE]":
                    yield StreamEvent(event=event_name, data=_decode_event_data(payload))
        finally:
            self.close()

    def iter_text(self) -> Iterator[str]:
        for event in self.iter_events():
            yield from _extract_text(event.data)


def _decode_event_data(payload: str) -> Any:
    try:
        return json.loads(payload)
    except json.JSONDecodeError:
        return payload


def _extract_text(payload: Any) -> Iterator[str]:
    if isinstance(payload, str):
        if payload:
            yield payload
        return

    if isinstance(payload, list):
        for item in payload:
            yield from _extract_text(item)
        return

    if not isinstance(payload, dict):
        return

    choices = payload.get("choices")
    if isinstance(choices, list):
        for choice in choices:
            if not isinstance(choice, dict):
                continue
            delta = choice.get("delta")
            if isinstance(delta, dict):
                content = delta.get("content")
                if isinstance(content, str) and content:
                    yield content
                yield from _extract_text(delta)
            message = choice.get("message")
            if isinstance(message, dict):
                yield from _extract_text(message)

    content = payload.get("content")
    if isinstance(content, str) and content:
        yield content
    elif isinstance(content, list):
        for item in content:
            yield from _extract_text(item)

    delta = payload.get("delta")
    if isinstance(delta, str) and delta:
        yield delta
    elif isinstance(delta, dict):
        text = delta.get("text")
        if isinstance(text, str) and text:
            yield text
        content = delta.get("content")
        if isinstance(content, str) and content:
            yield content

    text = payload.get("text")
    if isinstance(text, str) and text:
        yield text

    message = payload.get("message")
    if isinstance(message, dict):
        yield from _extract_text(message)

    output = payload.get("output")
    if isinstance(output, list):
        for item in output:
            yield from _extract_text(item)
