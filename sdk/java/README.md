# ima-router Java SDK

Java SDK for the current IMA Router LLM and multimodal endpoints.

This implementation uses only the JDK standard library:

- `java.net.http.HttpClient`
- a small local JSON utility

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
