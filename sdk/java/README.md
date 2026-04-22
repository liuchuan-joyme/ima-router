# ima-router Java SDK

Java SDK for the current IMA Router LLM and multimodal endpoints.

This implementation uses only the JDK standard library:

- `java.net.http.HttpClient`
- a small local JSON utility

## What This SDK Covers

- LLM: `/v1/chat/completions`, `/v1/responses`, `/v1/messages`
- Images: `/v1/images/generations`
- Videos: `/v1/videos`
- Kling: `/kling/v1/videos/text2video`, `/kling/v1/videos/image2video`
- Midjourney: `/mj/submit/imagine`, `/mj/task/{task_id}/fetch`

The platform supports **many more models than the ones shown in any quick example**.
This README is only meant to show representative SDK usage.

For the latest supported models, parameters, and capability updates, always check:

- [Official API docs](https://open-route-api.fashionlabs.cn/431672322e0)

## Compile

```bash
mkdir -p /tmp/ima-router-java-out
javac -d /tmp/ima-router-java-out $(find sdk/java/src/main/java -name '*.java')
```

## Example Import

```java
import com.imarouter.sdk.IMAClient;
import com.imarouter.sdk.Requests;
```

## Notes

- Do not treat the example model names as the full supported model list.
- New models may be available before this README is updated.
- Model-specific parameters should follow the official documentation and server-side validation.
