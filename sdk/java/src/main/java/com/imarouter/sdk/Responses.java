package com.imarouter.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Responses {
    private Responses() {}

    public static final class ChatCompletion {
        private final Map<String, Object> raw;

        public ChatCompletion(Map<String, Object> raw) {
            this.raw = raw;
        }

        public Map<String, Object> raw() {
            return raw;
        }

        public String text() {
            List<Object> choices = JsonUtil.asArray(raw.get("choices"));
            if (choices == null || choices.isEmpty()) {
                return "";
            }
            Map<String, Object> choice = JsonUtil.asObject(choices.getFirst());
            if (choice == null) {
                return "";
            }
            Map<String, Object> message = JsonUtil.asObject(choice.get("message"));
            if (message == null) {
                return "";
            }
            return JsonUtil.asString(message.get("content"));
        }

        public String toPrettyJson() {
            return JsonUtil.toPrettyJson(raw);
        }
    }

    public static final class ChatCompletionChunk {
        private final Map<String, Object> raw;

        public ChatCompletionChunk(Map<String, Object> raw) {
            this.raw = raw;
        }

        public Map<String, Object> raw() {
            return raw;
        }

        public String text() {
            StringBuilder builder = new StringBuilder();
            List<Object> choices = JsonUtil.asArray(raw.get("choices"));
            if (choices == null) {
                return "";
            }
            for (Object item : choices) {
                Map<String, Object> choice = JsonUtil.asObject(item);
                if (choice == null) {
                    continue;
                }
                Map<String, Object> delta = JsonUtil.asObject(choice.get("delta"));
                if (delta != null) {
                    String content = JsonUtil.asString(delta.get("content"));
                    if (content != null) {
                        builder.append(content);
                    }
                }
                Map<String, Object> message = JsonUtil.asObject(choice.get("message"));
                if (message != null) {
                    String content = JsonUtil.asString(message.get("content"));
                    if (content != null) {
                        builder.append(content);
                    }
                }
            }
            return builder.toString();
        }
    }

    public static final class ResponsesResult {
        private final Map<String, Object> raw;

        public ResponsesResult(Map<String, Object> raw) {
            this.raw = raw;
        }

        public Map<String, Object> raw() {
            return raw;
        }

        public String text() {
            StringBuilder builder = new StringBuilder();
            List<Object> outputs = JsonUtil.asArray(raw.get("output"));
            if (outputs == null) {
                return "";
            }
            for (Object outputItem : outputs) {
                Map<String, Object> output = JsonUtil.asObject(outputItem);
                if (output == null) {
                    continue;
                }
                List<Object> contents = JsonUtil.asArray(output.get("content"));
                if (contents == null) {
                    continue;
                }
                for (Object contentItem : contents) {
                    Map<String, Object> content = JsonUtil.asObject(contentItem);
                    if (content == null) {
                        continue;
                    }
                    String text = JsonUtil.asString(content.get("text"));
                    if (text != null) {
                        builder.append(text);
                    }
                }
            }
            return builder.toString();
        }

        public String toPrettyJson() {
            return JsonUtil.toPrettyJson(raw);
        }
    }

    public static final class AnthropicMessagesResult {
        private final Map<String, Object> raw;

        public AnthropicMessagesResult(Map<String, Object> raw) {
            this.raw = raw;
        }

        public Map<String, Object> raw() {
            return raw;
        }

        public String text() {
            StringBuilder builder = new StringBuilder();
            List<Object> contents = JsonUtil.asArray(raw.get("content"));
            if (contents == null) {
                return "";
            }
            for (Object item : contents) {
                Map<String, Object> block = JsonUtil.asObject(item);
                if (block == null) {
                    continue;
                }
                String text = JsonUtil.asString(block.get("text"));
                if (text != null) {
                    builder.append(text);
                }
            }
            return builder.toString();
        }

        public String toPrettyJson() {
            return JsonUtil.toPrettyJson(raw);
        }
    }

    public static final class ImageTaskSubmission {
        private final Map<String, Object> raw;

        public ImageTaskSubmission(Map<String, Object> raw) {
            this.raw = raw;
        }

        public Map<String, Object> raw() {
            return raw;
        }

        public String taskId() {
            return firstText("task_id", "id");
        }

        public String status() {
            return JsonUtil.asString(raw.get("status"));
        }

        public String toPrettyJson() {
            return JsonUtil.toPrettyJson(raw);
        }

        private String firstText(String... keys) {
            for (String key : keys) {
                String value = JsonUtil.asString(raw.get(key));
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
            return "";
        }
    }

    public static final class ImageTask {
        private final Map<String, Object> raw;

        public ImageTask(Map<String, Object> raw) {
            this.raw = raw;
        }

        public Map<String, Object> raw() {
            return raw;
        }

        public Map<String, Object> view() {
            Map<String, Object> data = JsonUtil.asObject(raw.get("data"));
            return data != null ? data : raw;
        }

        public String status() {
            return JsonUtil.asString(view().get("status"));
        }

        public Integer progress() {
            return JsonUtil.asInteger(view().get("progress"));
        }

        public String primaryUrl() {
            return JsonUtil.asString(view().get("url"));
        }

        public String toPrettyJson() {
            return JsonUtil.toPrettyJson(raw);
        }
    }

    public static final class VideoTask {
        private final Map<String, Object> raw;

        public VideoTask(Map<String, Object> raw) {
            this.raw = raw;
        }

        public Map<String, Object> raw() {
            return raw;
        }

        public Map<String, Object> view() {
            Map<String, Object> data = JsonUtil.asObject(raw.get("data"));
            return data != null ? data : raw;
        }

        public String taskId() {
            return firstText(view(), "task_id", "id");
        }

        public String status() {
            return JsonUtil.asString(view().get("status"));
        }

        public Integer progress() {
            return JsonUtil.asInteger(view().get("progress"));
        }

        public String primaryUrl() {
            Map<String, Object> view = view();
            List<Object> results = JsonUtil.asArray(view.get("results"));
            if (results != null) {
                for (Object item : results) {
                    Map<String, Object> result = JsonUtil.asObject(item);
                    if (result == null) {
                        continue;
                    }
                    String url = JsonUtil.asString(result.get("url"));
                    if (url != null && !url.isBlank()) {
                        return url;
                    }
                }
            }
            Map<String, Object> metadata = JsonUtil.asObject(view.get("metadata"));
            if (metadata != null) {
                String url = JsonUtil.asString(metadata.get("url"));
                if (url != null && !url.isBlank()) {
                    return url;
                }
            }
            return "";
        }

        public String toPrettyJson() {
            return JsonUtil.toPrettyJson(raw);
        }
    }

    public static final class MidjourneySubmit {
        private final Map<String, Object> raw;

        public MidjourneySubmit(Map<String, Object> raw) {
            this.raw = raw;
        }

        public Map<String, Object> raw() {
            return raw;
        }

        public String taskId() {
            return JsonUtil.asString(raw.get("result"));
        }

        public String toPrettyJson() {
            return JsonUtil.toPrettyJson(raw);
        }
    }

    public static final class MidjourneyTask {
        private final Map<String, Object> raw;

        public MidjourneyTask(Map<String, Object> raw) {
            this.raw = raw;
        }

        public Map<String, Object> raw() {
            return raw;
        }

        public String status() {
            return JsonUtil.asString(raw.get("status"));
        }

        public String progress() {
            return JsonUtil.asString(raw.get("progress"));
        }

        public List<String> urls() {
            List<Object> items = JsonUtil.asArray(raw.get("urls"));
            if (items == null) {
                return Collections.emptyList();
            }
            List<String> urls = new ArrayList<>();
            for (Object item : items) {
                String url = JsonUtil.asString(item);
                if (url != null && !url.isBlank()) {
                    urls.add(url);
                }
            }
            return urls;
        }

        public String imageUrl() {
            return JsonUtil.asString(raw.get("imageUrl"));
        }

        public String primaryUrl() {
            for (String url : urls()) {
                if (!url.isBlank()) {
                    return url;
                }
            }
            String imageUrl = imageUrl();
            return imageUrl == null ? "" : imageUrl;
        }

        public String failReason() {
            return JsonUtil.asString(raw.get("failReason"));
        }

        public String toPrettyJson() {
            return JsonUtil.toPrettyJson(raw);
        }
    }

    private static String firstText(Map<String, Object> source, String... keys) {
        if (source == null) {
            return "";
        }
        for (String key : keys) {
            String value = JsonUtil.asString(source.get(key));
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
