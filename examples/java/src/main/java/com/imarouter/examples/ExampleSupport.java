package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.JsonUtil;
import com.imarouter.sdk.Responses;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExampleSupport {
    private static final String DEFAULT_TEST_IMAGE_URL = "https://file.fashionlabs.cn/doc_image/r2v_tea_pic1.jpg";
    private static final String DEFAULT_HEAD_IMAGE_URL = "https://dev-jiman.oss-cn-hangzhou.aliyuncs.com/jm/20260316/ac5fe9ec6640426bafd6200b254b4b5f.png";
    private static final String DEFAULT_TAIL_IMAGE_URL = "https://dev-jiman.oss-cn-hangzhou.aliyuncs.com/jm/20260316/321175f98af24025b835debeb18002cc.png";

    private static final Map<String, String> DOT_ENV = loadDotEnv();

    private ExampleSupport() {}

    public static IMAClient createClient() {
        return new IMAClient(
            envOr("IMA_API_KEY", ""),
            envOr("IMA_BASE_URL", IMAClient.DEFAULT_BASE_URL),
            httpTimeout()
        );
    }

    public static String envOr(String key, String fallback) {
        String systemValue = System.getenv(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String dotEnvValue = DOT_ENV.get(key);
        if (dotEnvValue != null && !dotEnvValue.isBlank()) {
            return dotEnvValue;
        }

        return fallback;
    }

    public static int envInt(String key, int fallback) {
        try {
            return Integer.parseInt(envOr(key, String.valueOf(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public static boolean envBool(String key, boolean fallback) {
        String value = envOr(key, "");
        if (value.isBlank()) {
            return fallback;
        }
        return value.equalsIgnoreCase("true");
    }

    public static Duration httpTimeout() {
        return secondsDuration("IMA_HTTP_TIMEOUT_SECONDS", 300);
    }

    public static Duration imageWaitTimeout() {
        return secondsDuration("IMA_IMAGE_WAIT_TIMEOUT_SECONDS", 1800);
    }

    public static Duration imagePollInterval() {
        return secondsDuration("IMA_IMAGE_POLL_INTERVAL_SECONDS", 8);
    }

    public static Duration videoWaitTimeout() {
        return secondsDuration("IMA_VIDEO_WAIT_TIMEOUT_SECONDS", 3600);
    }

    public static Duration videoPollInterval() {
        return secondsDuration("IMA_VIDEO_POLL_INTERVAL_SECONDS", 10);
    }

    public static String testImageUrl() {
        return envOr("IMA_TEST_IMAGE_URL", DEFAULT_TEST_IMAGE_URL);
    }

    public static String testImageHeadUrl() {
        return envOr("IMA_TEST_IMAGE_HEAD_URL", testImageUrl().isBlank() ? DEFAULT_HEAD_IMAGE_URL : testImageUrl());
    }

    public static String testImageTailUrl() {
        return envOr("IMA_TEST_IMAGE_TAIL_URL", DEFAULT_TAIL_IMAGE_URL);
    }

    public static void printJson(String title, Object value) {
        System.out.println(title);
        System.out.println(JsonUtil.toPrettyJson(value));
    }

    public static void printImageOutcome(Responses.ImageTask task) {
        System.out.printf("status=%s%n", task.status());
        if (!task.primaryUrl().isBlank()) {
            System.out.printf("url=%s%n", task.primaryUrl());
        }
    }

    public static String imageResultUrl(Responses.ImageTask task) {
        return task.primaryUrl();
    }

    public static void printVideoOutcome(Responses.VideoTask task) {
        System.out.printf("status=%s%n", task.status());
        if (!task.primaryUrl().isBlank()) {
            System.out.printf("url=%s%n", task.primaryUrl());
        }
    }

    public static void printVideoPollStatus(Responses.VideoTask task, Integer attempt) {
        System.out.printf("[poll %d] status=%s progress=%s%n", attempt, task.status(), task.progress());
    }

    public static void printMidjourneyOutcome(Responses.MidjourneyTask task) {
        System.out.printf("status=%s%n", task.status());
        if (task.imageUrl() != null && !task.imageUrl().isBlank()) {
            System.out.printf("imageUrl=%s%n", task.imageUrl());
        }
        List<String> urls = task.urls();
        for (int i = 0; i < urls.size(); i++) {
            System.out.printf("urls[%d]=%s%n", i + 1, urls.get(i));
        }
        if (task.failReason() != null && !task.failReason().isBlank()) {
            System.out.printf("failReason=%s%n", task.failReason());
        }
    }

    public static String midjourneyResultUrl(Responses.MidjourneyTask task) {
        return task.primaryUrl();
    }

    private static Duration secondsDuration(String key, int fallbackSeconds) {
        return Duration.ofSeconds(envInt(key, fallbackSeconds));
    }

    private static Map<String, String> loadDotEnv() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path dotEnv = current.resolve(".env");
            if (Files.exists(dotEnv)) {
                return parseDotEnv(dotEnv);
            }
            current = current.getParent();
        }
        return Map.of();
    }

    private static Map<String, String> parseDotEnv(Path path) {
        try {
            Map<String, String> values = new LinkedHashMap<>();
            for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("export ")) {
                    line = line.substring("export ".length()).trim();
                }
                int separator = line.indexOf('=');
                if (separator < 0) {
                    continue;
                }
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                if (value.length() >= 2) {
                    char first = value.charAt(0);
                    char last = value.charAt(value.length() - 1);
                    if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                        value = value.substring(1, value.length() - 1);
                    }
                }
                values.put(key, value);
            }
            return values;
        } catch (IOException ignored) {
            return Map.of();
        }
    }
}
