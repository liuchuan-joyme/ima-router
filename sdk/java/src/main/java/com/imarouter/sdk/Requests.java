package com.imarouter.sdk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Requests {
    private Requests() {}

    public record ChatMessage(String role, String content) {
        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("role", role);
            map.put("content", content);
            return map;
        }
    }

    public record AnthropicMessage(String role, String content) {
        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("role", role);
            map.put("content", content);
            return map;
        }
    }

    public static final class ChatCompletion {
        private final Map<String, Object> payload = new LinkedHashMap<>();

        public ChatCompletion(String model, List<ChatMessage> messages) {
            payload.put("model", requireText(model, "model"));
            payload.put("messages", messagesToMaps(messages));
        }

        public ChatCompletion stream(boolean value) {
            if (value) {
                payload.put("stream", true);
            } else {
                payload.remove("stream");
            }
            return this;
        }

        public ChatCompletion temperature(double value) {
            payload.put("temperature", value);
            return this;
        }

        public ChatCompletion maxTokens(int value) {
            payload.put("max_tokens", value);
            return this;
        }

        public ChatCompletion tools(List<Map<String, Object>> value) {
            payload.put("tools", value);
            return this;
        }

        public ChatCompletion toolChoice(Object value) {
            payload.put("tool_choice", value);
            return this;
        }

        public Map<String, Object> toMap() {
            return new LinkedHashMap<>(payload);
        }
    }

    public static final class Responses {
        private final Map<String, Object> payload = new LinkedHashMap<>();

        public Responses(String model, String input) {
            payload.put("model", requireText(model, "model"));
            payload.put("input", requireText(input, "input"));
        }

        public Responses maxOutputTokens(int value) {
            payload.put("max_output_tokens", value);
            return this;
        }

        public Responses temperature(double value) {
            payload.put("temperature", value);
            return this;
        }

        public Responses stream(boolean value) {
            if (value) {
                payload.put("stream", true);
            } else {
                payload.remove("stream");
            }
            return this;
        }

        public Map<String, Object> toMap() {
            return new LinkedHashMap<>(payload);
        }
    }

    public static final class AnthropicMessages {
        private final Map<String, Object> payload = new LinkedHashMap<>();

        public AnthropicMessages(String model, int maxTokens, List<AnthropicMessage> messages) {
            payload.put("model", requireText(model, "model"));
            payload.put("max_tokens", maxTokens);
            payload.put("messages", anthropicMessagesToMaps(messages));
        }

        public AnthropicMessages system(String value) {
            if (value != null && !value.isBlank()) {
                payload.put("system", value);
            }
            return this;
        }

        public AnthropicMessages temperature(double value) {
            payload.put("temperature", value);
            return this;
        }

        public Map<String, Object> toMap() {
            return new LinkedHashMap<>(payload);
        }
    }

    public static final class GeminiImage {
        private final Map<String, Object> payload = new LinkedHashMap<>();

        private GeminiImage(String model, String prompt) {
            payload.put("model", requireText(model, "model"));
            payload.put("prompt", requireText(prompt, "prompt"));
        }

        public static GeminiImage textToImage(String model, String prompt) {
            return new GeminiImage(model, prompt);
        }

        public static GeminiImage imageToImage(String model, String prompt, List<String> images) {
            GeminiImage request = new GeminiImage(model, prompt);
            request.images(images);
            return request;
        }

        public GeminiImage aspectRatio(String value) {
            putIfText("aspect_ratio", value);
            return this;
        }

        public GeminiImage size(String value) {
            putIfText("size", value);
            return this;
        }

        public GeminiImage image(String value) {
            if (value != null && !value.isBlank()) {
                payload.put("image", value);
                payload.remove("images");
            }
            return this;
        }

        public GeminiImage images(List<String> values) {
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("at least one image url is required");
            }
            if (values.size() == 1) {
                return image(values.getFirst());
            }
            payload.put("images", new ArrayList<>(values));
            payload.remove("image");
            return this;
        }

        public Map<String, Object> toMap() {
            return new LinkedHashMap<>(payload);
        }

        private void putIfText(String key, String value) {
            if (value != null && !value.isBlank()) {
                payload.put(key, value);
            }
        }
    }

    public static final class SeedanceVideoMetadata {
        private final Map<String, Object> payload = new LinkedHashMap<>();

        public SeedanceVideoMetadata aspectRatio(String value) {
            putIfText("aspect_ratio", value);
            return this;
        }

        public SeedanceVideoMetadata resolution(String value) {
            putIfText("resolution", value);
            return this;
        }

        public SeedanceVideoMetadata audio(boolean value) {
            payload.put("audio", value);
            return this;
        }

        public SeedanceVideoMetadata audio(String value) {
            putIfText("audio", value);
            return this;
        }

        public SeedanceVideoMetadata roleMode(String value) {
            putIfText("role_mode", value);
            return this;
        }

        public SeedanceVideoMetadata referenceVideoUrls(List<String> values) {
            if (values != null && !values.isEmpty()) {
                payload.put("reference_video_urls", new ArrayList<>(values));
            }
            return this;
        }

        public SeedanceVideoMetadata referenceAudioUrls(List<String> values) {
            if (values != null && !values.isEmpty()) {
                payload.put("reference_audio_urls", new ArrayList<>(values));
            }
            return this;
        }

        public Map<String, Object> toMap() {
            return new LinkedHashMap<>(payload);
        }

        private void putIfText(String key, String value) {
            if (value != null && !value.isBlank()) {
                payload.put(key, value);
            }
        }
    }

    public static final class SeedanceVideo {
        private final Map<String, Object> payload = new LinkedHashMap<>();
        private SeedanceVideoMetadata metadata;

        private SeedanceVideo(String model) {
            payload.put("model", requireText(model, "model"));
        }

        public static SeedanceVideo textToVideo(String model, String prompt) {
            return new SeedanceVideo(model).prompt(prompt);
        }

        public static SeedanceVideo imageToVideo(String model, String imageUrl) {
            return new SeedanceVideo(model).images(List.of(requireText(imageUrl, "imageUrl")));
        }

        public SeedanceVideo prompt(String value) {
            if (value != null && !value.isBlank()) {
                payload.put("prompt", value);
            }
            return this;
        }

        public SeedanceVideo images(List<String> values) {
            if (values != null && !values.isEmpty()) {
                payload.put("images", new ArrayList<>(values));
            }
            return this;
        }

        public SeedanceVideo inputReference(String value) {
            if (value != null && !value.isBlank()) {
                payload.put("input_reference", value);
            }
            return this;
        }

        public SeedanceVideo duration(int value) {
            payload.put("duration", value);
            return this;
        }

        public SeedanceVideo seconds(String value) {
            if (value != null && !value.isBlank()) {
                payload.put("seconds", value);
            }
            return this;
        }

        public SeedanceVideo size(String value) {
            if (value != null && !value.isBlank()) {
                payload.put("size", value);
            }
            return this;
        }

        public SeedanceVideo metadata(SeedanceVideoMetadata value) {
            this.metadata = value;
            return this;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>(payload);
            if (metadata != null) {
                map.put("metadata", metadata.toMap());
            }
            return map;
        }
    }

    public static final class KlingTextToVideo {
        private final Map<String, Object> payload = new LinkedHashMap<>();

        public KlingTextToVideo(String model, String prompt) {
            payload.put("model", requireText(model, "model"));
            payload.put("prompt", requireText(prompt, "prompt"));
        }

        public KlingTextToVideo negativePrompt(String value) {
            putIfText("negative_prompt", value);
            return this;
        }

        public KlingTextToVideo mode(String value) {
            putIfText("mode", value);
            return this;
        }

        public KlingTextToVideo duration(int value) {
            payload.put("duration", value);
            return this;
        }

        public KlingTextToVideo aspectRatio(String value) {
            putIfText("aspect_ratio", value);
            return this;
        }

        public Map<String, Object> toMap() {
            return new LinkedHashMap<>(payload);
        }

        private void putIfText(String key, String value) {
            if (value != null && !value.isBlank()) {
                payload.put(key, value);
            }
        }
    }

    public static final class KlingImageToVideo {
        private final Map<String, Object> payload = new LinkedHashMap<>();

        public KlingImageToVideo(String model, String imageUrl) {
            payload.put("model", requireText(model, "model"));
            payload.put("image", requireText(imageUrl, "imageUrl"));
        }

        public KlingImageToVideo imageTail(String value) {
            putIfText("image_tail", value);
            return this;
        }

        public KlingImageToVideo prompt(String value) {
            putIfText("prompt", value);
            return this;
        }

        public KlingImageToVideo negativePrompt(String value) {
            putIfText("negative_prompt", value);
            return this;
        }

        public KlingImageToVideo mode(String value) {
            putIfText("mode", value);
            return this;
        }

        public KlingImageToVideo duration(int value) {
            payload.put("duration", value);
            return this;
        }

        public KlingImageToVideo aspectRatio(String value) {
            putIfText("aspect_ratio", value);
            return this;
        }

        public Map<String, Object> toMap() {
            return new LinkedHashMap<>(payload);
        }

        private void putIfText(String key, String value) {
            if (value != null && !value.isBlank()) {
                payload.put(key, value);
            }
        }
    }

    public static final class MidjourneyImagine {
        private final String prompt;

        private MidjourneyImagine(String prompt) {
            this.prompt = requireText(prompt, "prompt");
        }

        public static MidjourneyImagine niji(String prompt, int version, String aspectRatio, String extraArgs) {
            int resolvedVersion = version == 0 ? 7 : version;
            List<String> parts = new ArrayList<>();
            parts.add(requireText(prompt, "prompt"));
            parts.add("--niji " + resolvedVersion);
            if (aspectRatio != null && !aspectRatio.isBlank()) {
                parts.add("--ar " + aspectRatio);
            }
            if (extraArgs != null && !extraArgs.isBlank()) {
                parts.add(extraArgs.trim());
            }
            return new MidjourneyImagine(String.join(" ", parts));
        }

        public static MidjourneyImagine nijiWithImage(String imageUrl, String prompt, int version, String aspectRatio, String extraArgs) {
            int resolvedVersion = version == 0 ? 7 : version;
            List<String> parts = new ArrayList<>();
            parts.add(requireText(imageUrl, "imageUrl"));
            parts.add(requireText(prompt, "prompt"));
            parts.add("--niji " + resolvedVersion);
            if (aspectRatio != null && !aspectRatio.isBlank()) {
                parts.add("--ar " + aspectRatio);
            }
            if (extraArgs != null && !extraArgs.isBlank()) {
                parts.add(extraArgs.trim());
            }
            return new MidjourneyImagine(String.join(" ", parts));
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("prompt", prompt);
            return map;
        }
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name + " is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value;
    }

    private static List<Map<String, Object>> messagesToMaps(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages are required");
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (ChatMessage message : messages) {
            list.add(message.toMap());
        }
        return list;
    }

    private static List<Map<String, Object>> anthropicMessagesToMaps(List<AnthropicMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages are required");
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (AnthropicMessage message : messages) {
            list.add(message.toMap());
        }
        return list;
    }
}
