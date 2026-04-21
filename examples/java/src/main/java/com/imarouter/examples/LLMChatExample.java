package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

import java.util.List;

public final class LLMChatExample {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();
        var response = client.chatCompletions().create(
            new Requests.ChatCompletion(
                ExampleSupport.envOr("IMA_CHAT_MODEL", "gpt-5.3-codex"),
                List.of(
                    new Requests.ChatMessage("system", "You are a concise assistant."),
                    new Requests.ChatMessage("user", "Introduce ima-router in 3 short bullet points.")
                )
            )
        );
        System.out.println(response.text());
    }
}
