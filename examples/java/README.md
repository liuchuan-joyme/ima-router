# Java Examples

These examples use the local Java SDK and automatically load `.env` from the repository root.

## Compile

```bash
mkdir -p /tmp/ima-router-java-out
javac -d /tmp/ima-router-java-out $(find sdk/java/src/main/java examples/java/src/main/java -name '*.java')
```

## Run

```bash
java -cp /tmp/ima-router-java-out com.imarouter.examples.LLMChatExample
java -cp /tmp/ima-router-java-out com.imarouter.examples.LLMChatStreamExample
java -cp /tmp/ima-router-java-out com.imarouter.examples.LLMResponsesExample
java -cp /tmp/ima-router-java-out com.imarouter.examples.LLMMessagesExample
java -cp /tmp/ima-router-java-out com.imarouter.examples.LLMChatMatrixExample

java -cp /tmp/ima-router-java-out com.imarouter.examples.ImageBananaProExample
java -cp /tmp/ima-router-java-out com.imarouter.examples.ImageBanana2Example
java -cp /tmp/ima-router-java-out com.imarouter.examples.ImageMidjourneyNiji7Example

java -cp /tmp/ima-router-java-out com.imarouter.examples.VideoSD2Text2VideoExample
java -cp /tmp/ima-router-java-out com.imarouter.examples.VideoSD2Image2VideoExample
java -cp /tmp/ima-router-java-out com.imarouter.examples.VideoKlingText2VideoExample
java -cp /tmp/ima-router-java-out com.imarouter.examples.VideoKlingImage2VideoExample
```
