package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

import java.util.List;

public final class LLMChatMatrixExample {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();
        String[] models = ExampleSupport.envOr(
            "IMA_CHAT_MODELS",
            "gpt-5.3-codex,gpt-5.4,claude-sonnet-4-6,claude-opus-4-6"
        ).split(",");

        for (String rawModel : models) {
            String model = rawModel.trim();
            if (model.isEmpty()) {
                continue;
            }
            System.out.printf("=== %s ===%n", model);
            try {
                var response = client.chatCompletions().create(
                    new Requests.ChatCompletion(
                        model,
                        List.of(new Requests.ChatMessage("user", "Reply with exactly: OK " + model))
                    ).temperature(0).maxTokens(120)
                );
                System.out.println(response.text());
            } catch (RuntimeException error) {
                System.out.printf("ERROR: %s%n", error.getMessage());
            }
            System.out.println();
        }
    }
}
