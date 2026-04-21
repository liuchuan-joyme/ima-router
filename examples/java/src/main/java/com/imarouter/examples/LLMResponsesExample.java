package com.imarouter.examples;

import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;

public final class LLMResponsesExample {
    public static void main(String[] args) {
        IMAClient client = ExampleSupport.createClient();
        var response = client.responses().create(
            new Requests.Responses(
                ExampleSupport.envOr("IMA_RESPONSES_MODEL", "gpt-5.3-codex"),
                "Write a one paragraph Java doc comment for an ima-router client."
            ).maxOutputTokens(256)
        );
        System.out.println(response.text());
    }
}
