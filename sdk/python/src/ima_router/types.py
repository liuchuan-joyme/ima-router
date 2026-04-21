from __future__ import annotations

from typing import Any, Literal, TypedDict

JSONDict = dict[str, Any]


class ChatMessage(TypedDict):
    role: Literal["system", "user", "assistant"]
    content: str


class AnthropicMessage(TypedDict):
    role: Literal["user", "assistant"]
    content: str


class FunctionDefinition(TypedDict, total=False):
    name: str
    description: str
    parameters: dict[str, Any]


class Tool(TypedDict):
    type: Literal["function"]
    function: FunctionDefinition


class ToolChoiceFunction(TypedDict):
    name: str


class ToolChoiceObject(TypedDict):
    type: Literal["function"]
    function: ToolChoiceFunction
