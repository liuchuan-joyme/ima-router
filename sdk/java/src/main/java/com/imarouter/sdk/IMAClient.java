package com.imarouter.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class IMAClient {
    public static final String DEFAULT_BASE_URL = "https://api.imarouter.com";
    public static final String DEFAULT_ANTHROPIC_VERSION = "2023-06-01";

    private final String baseUrl;
    private final String apiKey;
    private final Duration timeout;
    private final HttpClient httpClient;

    private final ChatCompletionsService chatCompletions;
    private final ResponsesService responses;
    private final MessagesService messages;
    private final ImagesService images;
    private final VideosService videos;
    private final KlingService kling;
    private final MidjourneyService midjourney;

    public IMAClient(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL, Duration.ofSeconds(60));
    }

    public IMAClient(String apiKey, String baseUrl, Duration timeout) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("api key is required");
        }

        this.apiKey = apiKey;
        this.baseUrl = (baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl).replaceAll("/+$", "");
        this.timeout = timeout == null || timeout.isZero() || timeout.isNegative() ? Duration.ofSeconds(60) : timeout;
        this.httpClient = HttpClient.newBuilder().connectTimeout(this.timeout).build();

        this.chatCompletions = new ChatCompletionsService();
        this.responses = new ResponsesService();
        this.messages = new MessagesService();
        this.images = new ImagesService();
        this.videos = new VideosService();
        this.kling = new KlingService();
        this.midjourney = new MidjourneyService();
    }

    public ChatCompletionsService chatCompletions() {
        return chatCompletions;
    }

    public ResponsesService responses() {
        return responses;
    }

    public MessagesService messages() {
        return messages;
    }

    public ImagesService images() {
        return images;
    }

    public VideosService videos() {
        return videos;
    }

    public KlingService kling() {
        return kling;
    }

    public MidjourneyService midjourney() {
        return midjourney;
    }

    private HttpRequest buildRequest(String method, String path, Object payload, Map<String, String> headers) {
        HttpRequest.BodyPublisher body = payload == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(payload), StandardCharsets.UTF_8);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(buildURL(path)))
            .timeout(timeout)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .method(method, body);

        if (headers != null) {
            headers.forEach(builder::header);
        }

        return builder.build();
    }

    private String buildURL(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        if (baseUrl.endsWith("/v1") && normalized.startsWith("/v1/")) {
            normalized = normalized.substring(3);
        }
        return baseUrl + normalized;
    }

    private Map<String, Object> sendJSON(String method, String path, Object payload, Map<String, String> headers) {
        HttpRequest request = buildRequest(method, path, payload, headers);
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw parseAPIException(response.statusCode(), response.body());
            }
            Object parsed = JsonUtil.parse(response.body());
            Map<String, Object> object = JsonUtil.asObject(parsed);
            if (object == null) {
                throw new IllegalStateException("Expected JSON object response");
            }
            return object;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private byte[] sendBinary(String method, String path, Object payload, Map<String, String> headers) {
        HttpRequest request = buildRequest(method, path, payload, headers);
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 400) {
                throw parseAPIException(response.statusCode(), new String(response.body(), StandardCharsets.UTF_8));
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void streamSSE(String method, String path, Object payload, Map<String, String> headers, SSEHandler handler) {
        Objects.requireNonNull(handler, "stream handler is required");

        Map<String, String> mergedHeaders = new LinkedHashMap<>();
        if (headers != null) {
            mergedHeaders.putAll(headers);
        }
        mergedHeaders.put("Accept", "text/event-stream");

        HttpRequest request = buildRequest(method, path, payload, mergedHeaders);
        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw parseAPIException(response.statusCode(), body);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                String eventName = "";
                StringJoiner data = new StringJoiner("\n");

                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        if (!flushSSE(handler, eventName, data.toString())) {
                            return;
                        }
                        eventName = "";
                        data = new StringJoiner("\n");
                        continue;
                    }
                    if (line.startsWith(":")) {
                        continue;
                    }
                    if (line.startsWith("event:")) {
                        eventName = line.substring("event:".length()).trim();
                        continue;
                    }
                    if (line.startsWith("data:")) {
                        data.add(line.substring("data:".length()).stripLeading());
                    }
                }

                flushSSE(handler, eventName, data.toString());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private boolean flushSSE(SSEHandler handler, String eventName, String rawData) {
        if (rawData == null || rawData.isBlank()) {
            return true;
        }
        if ("[DONE]".equals(rawData)) {
            return false;
        }
        handler.handle(eventName, JsonUtil.asObject(JsonUtil.parse(rawData)));
        return true;
    }

    private APIException parseAPIException(int statusCode, String body) {
        Object parsed = null;
        try {
            parsed = JsonUtil.parse(body);
        } catch (RuntimeException ignored) {
        }
        String message = extractErrorMessage(parsed);
        if (message == null || message.isBlank()) {
            message = body == null || body.isBlank()
                ? "IMA Router request failed with status " + statusCode
                : body.strip();
        }
        return new APIException(statusCode, message, parsed);
    }

    private String extractErrorMessage(Object parsed) {
        Map<String, Object> payload = JsonUtil.asObject(parsed);
        if (payload == null) {
            return null;
        }
        Object rawError = payload.get("error");
        if (rawError instanceof String string && !string.isBlank()) {
            return string;
        }
        Map<String, Object> errorObject = JsonUtil.asObject(rawError);
        if (errorObject != null) {
            String message = JsonUtil.asString(errorObject.get("message"));
            if (message != null && !message.isBlank()) {
                return message;
            }
            String type = JsonUtil.asString(errorObject.get("type"));
            if (type != null && !type.isBlank()) {
                return type;
            }
        }
        String message = JsonUtil.asString(payload.get("message"));
        if (message != null && !message.isBlank()) {
            return message;
        }
        return null;
    }

    private static <T> T pollUntilTerminal(
        Function<String, T> fetcher,
        String taskId,
        Duration timeout,
        Duration interval,
        Predicate<T> terminal,
        BiConsumer<T, Integer> onPoll
    ) {
        Duration waitTimeout = timeout == null || timeout.isZero() || timeout.isNegative()
            ? Duration.ofMinutes(30)
            : timeout;
        Duration waitInterval = interval == null || interval.isZero() || interval.isNegative()
            ? Duration.ofSeconds(5)
            : interval;

        long deadline = System.nanoTime() + waitTimeout.toNanos();
        int attempt = 0;

        while (true) {
            T result = fetcher.apply(taskId);
            attempt += 1;
            if (onPoll != null) {
                onPoll.accept(result, attempt);
            }
            if (terminal.test(result)) {
                return result;
            }
            if (System.nanoTime() > deadline) {
                throw new RuntimeException("Timed out while waiting for task " + taskId);
            }
            try {
                Thread.sleep(waitInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    @FunctionalInterface
    private interface SSEHandler {
        void handle(String eventName, Map<String, Object> data);
    }

    public static final class APIException extends RuntimeException {
        private final int statusCode;
        private final Object body;

        public APIException(int statusCode, String message, Object body) {
            super(message);
            this.statusCode = statusCode;
            this.body = body;
        }

        public int statusCode() {
            return statusCode;
        }

        public Object body() {
            return body;
        }
    }

    public final class ChatCompletionsService {
        public Responses.ChatCompletion create(Requests.ChatCompletion request) {
            return new Responses.ChatCompletion(sendJSON("POST", "/v1/chat/completions", request.toMap(), null));
        }

        public void stream(Requests.ChatCompletion request, java.util.function.Consumer<Responses.ChatCompletionChunk> handler) {
            Requests.ChatCompletion streamRequest = new Requests.ChatCompletion(
                JsonUtil.asString(request.toMap().get("model")),
                toChatMessages(request.toMap().get("messages"))
            ).stream(true);
            Map<String, Object> map = request.toMap();
            if (map.containsKey("temperature")) {
                streamRequest.temperature(JsonUtil.asDouble(map.get("temperature")));
            }
            if (map.containsKey("max_tokens")) {
                streamRequest.maxTokens(JsonUtil.asInteger(map.get("max_tokens")));
            }
            streamSSE("POST", "/v1/chat/completions", streamRequest.toMap(), null, (event, data) -> handler.accept(new Responses.ChatCompletionChunk(data)));
        }
    }

    public final class ResponsesService {
        public com.imarouter.sdk.Responses.ResponsesResult create(Requests.Responses request) {
            return new com.imarouter.sdk.Responses.ResponsesResult(sendJSON("POST", "/v1/responses", request.toMap(), null));
        }
    }

    public final class MessagesService {
        public com.imarouter.sdk.Responses.AnthropicMessagesResult create(Requests.AnthropicMessages request) {
            return create(request, DEFAULT_ANTHROPIC_VERSION);
        }

        public com.imarouter.sdk.Responses.AnthropicMessagesResult create(Requests.AnthropicMessages request, String anthropicVersion) {
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("anthropic-version", anthropicVersion == null || anthropicVersion.isBlank()
                ? DEFAULT_ANTHROPIC_VERSION
                : anthropicVersion);
            return new com.imarouter.sdk.Responses.AnthropicMessagesResult(sendJSON("POST", "/v1/messages", request.toMap(), headers));
        }
    }

    public final class ImagesService {
        public com.imarouter.sdk.Responses.ImageTaskSubmission generate(Requests.GeminiImage request) {
            return new com.imarouter.sdk.Responses.ImageTaskSubmission(sendJSON("POST", "/v1/images/generations", request.toMap(), null));
        }

        public com.imarouter.sdk.Responses.ImageTask get(String taskId) {
            return new com.imarouter.sdk.Responses.ImageTask(sendJSON("GET", "/v1/images/generations/" + taskId, null, null));
        }

        public com.imarouter.sdk.Responses.ImageTask waitForTask(
            String taskId,
            Duration timeout,
            Duration interval,
            BiConsumer<com.imarouter.sdk.Responses.ImageTask, Integer> onPoll
        ) {
            return pollUntilTerminal(
                this::get,
                taskId,
                timeout,
                interval,
                task -> {
                    String status = task.status();
                    return "succeeded".equals(status) || "failed".equals(status);
                },
                onPoll
            );
        }
    }

    public final class VideosService {
        public com.imarouter.sdk.Responses.VideoTask create(Requests.SeedanceVideo request) {
            return new com.imarouter.sdk.Responses.VideoTask(sendJSON("POST", "/v1/videos", request.toMap(), null));
        }

        public com.imarouter.sdk.Responses.VideoTask get(String taskId) {
            return new com.imarouter.sdk.Responses.VideoTask(sendJSON("GET", "/v1/videos/" + taskId, null, null));
        }

        public com.imarouter.sdk.Responses.VideoTask waitForTask(
            String taskId,
            Duration timeout,
            Duration interval,
            BiConsumer<com.imarouter.sdk.Responses.VideoTask, Integer> onPoll
        ) {
            return pollUntilTerminal(
                this::get,
                taskId,
                timeout,
                interval,
                task -> {
                    String status = task.status();
                    return "completed".equals(status) || "failed".equals(status);
                },
                onPoll
            );
        }
    }

    public final class KlingService {
        public com.imarouter.sdk.Responses.VideoTask textToVideo(Requests.KlingTextToVideo request) {
            return new com.imarouter.sdk.Responses.VideoTask(sendJSON("POST", "/kling/v1/videos/text2video", request.toMap(), null));
        }

        public com.imarouter.sdk.Responses.VideoTask imageToVideo(Requests.KlingImageToVideo request) {
            return new com.imarouter.sdk.Responses.VideoTask(sendJSON("POST", "/kling/v1/videos/image2video", request.toMap(), null));
        }

        public com.imarouter.sdk.Responses.VideoTask get(String taskId) {
            return new com.imarouter.sdk.Responses.VideoTask(sendJSON("GET", "/v1/videos/" + taskId, null, null));
        }

        public com.imarouter.sdk.Responses.VideoTask waitForTask(
            String taskId,
            Duration timeout,
            Duration interval,
            BiConsumer<com.imarouter.sdk.Responses.VideoTask, Integer> onPoll
        ) {
            return pollUntilTerminal(
                this::get,
                taskId,
                timeout,
                interval,
                task -> {
                    String status = task.status();
                    return "completed".equals(status) || "failed".equals(status);
                },
                onPoll
            );
        }
    }

    public final class MidjourneyService {
        public com.imarouter.sdk.Responses.MidjourneySubmit imagine(Requests.MidjourneyImagine request) {
            return imagine(request, "");
        }

        public com.imarouter.sdk.Responses.MidjourneySubmit imagine(Requests.MidjourneyImagine request, String xYouchuanSetting) {
            Map<String, String> headers = new LinkedHashMap<>();
            if (xYouchuanSetting != null && !xYouchuanSetting.isBlank()) {
                headers.put("x-youchuan-setting", xYouchuanSetting);
            }
            return new com.imarouter.sdk.Responses.MidjourneySubmit(sendJSON("POST", "/mj/submit/imagine", request.toMap(), headers));
        }

        public com.imarouter.sdk.Responses.MidjourneyTask get(String taskId) {
            return new com.imarouter.sdk.Responses.MidjourneyTask(sendJSON("GET", "/mj/task/" + taskId + "/fetch", null, null));
        }

        public com.imarouter.sdk.Responses.MidjourneyTask waitForTask(
            String taskId,
            Duration timeout,
            Duration interval,
            BiConsumer<com.imarouter.sdk.Responses.MidjourneyTask, Integer> onPoll
        ) {
            return pollUntilTerminal(
                this::get,
                taskId,
                timeout,
                interval,
                task -> {
                    String status = task.status();
                    return "SUCCESS".equals(status) || "FAILURE".equals(status);
                },
                onPoll
            );
        }

        public byte[] fetchImage(String taskId) {
            return sendBinary("GET", "/mj/image/" + taskId, null, null);
        }
    }

    private static List<Requests.ChatMessage> toChatMessages(Object value) {
        List<Object> items = JsonUtil.asArray(value);
        if (items == null) {
            return List.of();
        }
        List<Requests.ChatMessage> messages = new java.util.ArrayList<>();
        for (Object item : items) {
            Map<String, Object> message = JsonUtil.asObject(item);
            if (message == null) {
                continue;
            }
            messages.add(new Requests.ChatMessage(
                JsonUtil.asString(message.get("role")),
                JsonUtil.asString(message.get("content"))
            ));
        }
        return messages;
    }
}
