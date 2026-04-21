from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Mapping, Sequence

ImageModel = str
SeedanceModel = str
KlingModel = str

Payload = dict[str, Any]


def _drop_none(values: Mapping[str, Any]) -> Payload:
    return {key: value for key, value in values.items() if value is not None}


def _as_list(values: str | Sequence[str] | None) -> list[str] | None:
    if values is None:
        return None
    if isinstance(values, str):
        return [values]
    return list(values)


@dataclass(slots=True)
class GeminiImageRequest:
    model: ImageModel
    prompt: str
    aspect_ratio: str | None = None
    size: str | None = None
    image: str | None = None
    images: Sequence[str] | None = None

    @classmethod
    def text_to_image(
        cls,
        *,
        model: ImageModel,
        prompt: str,
        aspect_ratio: str | None = None,
        size: str | None = None,
    ) -> "GeminiImageRequest":
        return cls(model=model, prompt=prompt, aspect_ratio=aspect_ratio, size=size)

    @classmethod
    def image_to_image(
        cls,
        *,
        model: ImageModel,
        prompt: str,
        images: str | Sequence[str],
        aspect_ratio: str | None = None,
        size: str | None = None,
    ) -> "GeminiImageRequest":
        image_list = _as_list(images)
        if image_list is None or not image_list:
            raise ValueError("At least one image URL is required for image-to-image.")

        if len(image_list) == 1:
            return cls(
                model=model,
                prompt=prompt,
                aspect_ratio=aspect_ratio,
                size=size,
                image=image_list[0],
            )

        return cls(
            model=model,
            prompt=prompt,
            aspect_ratio=aspect_ratio,
            size=size,
            images=image_list,
        )

    def to_payload(self) -> Payload:
        self._validate()
        payload = _drop_none(
            {
                "model": self.model,
                "prompt": self.prompt,
                "aspect_ratio": self.aspect_ratio,
                "size": self.size,
                "image": self.image,
                "images": list(self.images) if self.images is not None else None,
            }
        )
        return payload

    def _validate(self) -> None:
        if not self.prompt.strip():
            raise ValueError("prompt is required.")
        if self.image and self.images:
            raise ValueError("Use either image or images, not both.")


@dataclass(slots=True)
class SeedanceVideoMetadata:
    aspect_ratio: str | None = None
    resolution: str | None = None
    audio: bool | str | None = None
    role_mode: str | None = None
    reference_video_urls: Sequence[str] | None = None
    reference_audio_urls: Sequence[str] | None = None
    mcp_list: Sequence[Mapping[str, Any]] | None = None

    def to_payload(self) -> Payload:
        return _drop_none(
            {
                "aspect_ratio": self.aspect_ratio,
                "resolution": self.resolution,
                "audio": self.audio,
                "role_mode": self.role_mode,
                "reference_video_urls": list(self.reference_video_urls)
                if self.reference_video_urls is not None
                else None,
                "reference_audio_urls": list(self.reference_audio_urls)
                if self.reference_audio_urls is not None
                else None,
                "mcp_list": list(self.mcp_list) if self.mcp_list is not None else None,
            }
        )


@dataclass(slots=True)
class SeedanceVideoRequest:
    model: SeedanceModel
    prompt: str | None = None
    images: Sequence[str] | None = None
    input_reference: str | None = None
    duration: int | None = None
    seconds: str | None = None
    size: str | None = None
    metadata: SeedanceVideoMetadata | Mapping[str, Any] | None = None

    @classmethod
    def text_to_video(
        cls,
        *,
        model: SeedanceModel,
        prompt: str,
        duration: int = 5,
        aspect_ratio: str | None = "16:9",
        resolution: str | None = "720p",
        audio: bool | str | None = False,
    ) -> "SeedanceVideoRequest":
        return cls(
            model=model,
            prompt=prompt,
            duration=duration,
            metadata=SeedanceVideoMetadata(
                aspect_ratio=aspect_ratio,
                resolution=resolution,
                audio=audio,
            ),
        )

    @classmethod
    def image_to_video(
        cls,
        *,
        model: SeedanceModel,
        images: str | Sequence[str],
        prompt: str | None = None,
        duration: int = 5,
        aspect_ratio: str | None = "16:9",
        resolution: str | None = "720p",
        audio: bool | str | None = False,
        role_mode: Literal["reference", "frame"] = "reference",
    ) -> "SeedanceVideoRequest":
        image_list = _as_list(images)
        if image_list is None or not image_list:
            raise ValueError("At least one image URL is required for image-to-video.")
        return cls(
            model=model,
            prompt=prompt,
            images=image_list,
            duration=duration,
            metadata=SeedanceVideoMetadata(
                aspect_ratio=aspect_ratio,
                resolution=resolution,
                audio=audio,
                role_mode=role_mode,
            ),
        )

    def to_payload(self) -> Payload:
        self._validate()
        metadata = self._metadata_to_payload()
        return _drop_none(
            {
                "model": self.model,
                "prompt": self.prompt,
                "images": list(self.images) if self.images is not None else None,
                "input_reference": self.input_reference,
                "duration": self.duration,
                "seconds": self.seconds,
                "size": self.size,
                "metadata": metadata,
            }
        )

    def _metadata_to_payload(self) -> Payload | None:
        if self.metadata is None:
            return None
        if hasattr(self.metadata, "to_payload"):
            return self.metadata.to_payload()  # type: ignore[return-value]
        return _drop_none(dict(self.metadata))

    def _validate(self) -> None:
        image_list = list(self.images) if self.images is not None else []
        metadata = self._metadata_to_payload() or {}

        has_prompt = bool(self.prompt and self.prompt.strip())
        has_images = bool(image_list)
        has_reference_video = bool(metadata.get("reference_video_urls"))
        has_reference_audio = bool(metadata.get("reference_audio_urls"))

        if not any([has_prompt, has_images, has_reference_video, has_reference_audio]):
            raise ValueError(
                "Seedance video requests need prompt, images, reference video, or reference audio."
            )


@dataclass(slots=True)
class KlingCameraControl:
    type: str
    config: Mapping[str, Any]

    def to_payload(self) -> Payload:
        return {"type": self.type, "config": dict(self.config)}


@dataclass(slots=True)
class KlingTextToVideoRequest:
    model: KlingModel
    prompt: str
    negative_prompt: str | None = None
    mode: str | None = None
    duration: int | None = None
    aspect_ratio: str | None = None
    cfg_scale: float | None = None
    camera_control: KlingCameraControl | Mapping[str, Any] | None = None

    def to_payload(self) -> Payload:
        self._validate()
        return _drop_none(
            {
                "model": self.model,
                "prompt": self.prompt,
                "negative_prompt": self.negative_prompt,
                "mode": self.mode,
                "duration": self.duration,
                "aspect_ratio": self.aspect_ratio,
                "cfg_scale": self.cfg_scale,
                "camera_control": self._camera_control_payload(),
            }
        )

    def _camera_control_payload(self) -> Payload | None:
        if self.camera_control is None:
            return None
        if hasattr(self.camera_control, "to_payload"):
            return self.camera_control.to_payload()  # type: ignore[return-value]
        return dict(self.camera_control)

    def _validate(self) -> None:
        if not self.prompt.strip():
            raise ValueError("prompt is required for Kling text-to-video.")


@dataclass(slots=True)
class KlingImageToVideoRequest:
    model: KlingModel
    image: str
    prompt: str | None = None
    image_tail: str | None = None
    negative_prompt: str | None = None
    mode: str | None = None
    duration: int | None = None
    aspect_ratio: str | None = None
    cfg_scale: float | None = None
    camera_control: KlingCameraControl | Mapping[str, Any] | None = None

    def to_payload(self) -> Payload:
        self._validate()
        return _drop_none(
            {
                "model": self.model,
                "image": self.image,
                "prompt": self.prompt,
                "image_tail": self.image_tail,
                "negative_prompt": self.negative_prompt,
                "mode": self.mode,
                "duration": self.duration,
                "aspect_ratio": self.aspect_ratio,
                "cfg_scale": self.cfg_scale,
                "camera_control": self._camera_control_payload(),
            }
        )

    def _camera_control_payload(self) -> Payload | None:
        if self.camera_control is None:
            return None
        if hasattr(self.camera_control, "to_payload"):
            return self.camera_control.to_payload()  # type: ignore[return-value]
        return dict(self.camera_control)

    def _validate(self) -> None:
        if not self.image.strip():
            raise ValueError("image is required for Kling image-to-video.")


@dataclass(slots=True)
class MidjourneyImagineRequest:
    prompt: str

    @classmethod
    def niji(
        cls,
        prompt: str,
        *,
        version: int = 7,
        aspect_ratio: str | None = None,
        extra_args: str | None = None,
    ) -> "MidjourneyImagineRequest":
        prompt_parts = [prompt.strip(), f"--niji {version}"]
        if aspect_ratio:
            prompt_parts.append(f"--ar {aspect_ratio}")
        if extra_args:
            prompt_parts.append(extra_args.strip())
        return cls(prompt=" ".join(part for part in prompt_parts if part))

    @classmethod
    def niji_with_image(
        cls,
        image_url: str,
        prompt: str,
        *,
        version: int = 7,
        aspect_ratio: str | None = None,
        extra_args: str | None = None,
    ) -> "MidjourneyImagineRequest":
        if not image_url.strip():
            raise ValueError("image_url is required for Midjourney image-to-image.")

        prompt_parts = [image_url.strip(), prompt.strip(), f"--niji {version}"]
        if aspect_ratio:
            prompt_parts.append(f"--ar {aspect_ratio}")
        if extra_args:
            prompt_parts.append(extra_args.strip())
        return cls(prompt=" ".join(part for part in prompt_parts if part))

    def to_payload(self) -> Payload:
        if not self.prompt.strip():
            raise ValueError("prompt is required for Midjourney imagine.")
        return {"prompt": self.prompt}


@dataclass(slots=True)
class MidjourneyBlendRequest:
    images: Sequence[str]
    prompt: str | None = None

    def to_payload(self) -> Payload:
        image_list = list(self.images)
        if not 2 <= len(image_list) <= 5:
            raise ValueError("Midjourney blend requires between 2 and 5 image URLs.")
        return _drop_none({"base64Array": image_list, "prompt": self.prompt})


@dataclass(slots=True)
class MidjourneyChangeRequest:
    action: str
    task_id: str
    index: int | None = None
    prompt: str | None = None
    mask_base64: str | None = None
    custom_id: str | None = None

    def to_payload(self) -> Payload:
        if not self.task_id.strip():
            raise ValueError("task_id is required for Midjourney change.")
        if self.action != "REROLL" and self.index is None:
            raise ValueError("index is required unless the action is REROLL.")
        if self.action == "REMIX" and not (self.prompt and self.prompt.strip()):
            raise ValueError("prompt is required for REMIX.")

        payload = _drop_none(
            {
                "action": self.action,
                "taskId": self.task_id,
                "index": self.index,
                "prompt": self.prompt,
                "maskBase64": self.mask_base64,
                "customId": self.custom_id,
            }
        )
        return payload
