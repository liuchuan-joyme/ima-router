package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

import java.util.List;

public final class LLMChatStreamExample {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();
        client.chatCompletions().stream(
            new Requests.ChatCompletion(
                ExampleSupport.envOr("IMA_STREAM_MODEL", "gpt-5.4"),
                List.of(new Requests.ChatMessage("user", "Write a short launch tagline for ima-router."))
            ),
            chunk -> System.out.print(chunk.text())
        );
        System.out.println();
    }
}
