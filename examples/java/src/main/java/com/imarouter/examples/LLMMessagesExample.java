package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

import java.util.List;

public final class LLMMessagesExample {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();
        var response = client.messages().create(
            new Requests.AnthropicMessages(
                ExampleSupport.envOr("IMA_ANTHROPIC_MODEL", "claude-sonnet-4-6"),
                256,
                List.of(new Requests.AnthropicMessage("user", "Explain when I should use the messages endpoint."))
            ).system("You are a helpful assistant for ima-router users.")
        );
        System.out.println(response.text());
    }
}
