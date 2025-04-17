package io.akka.ai.application;

import akka.javasdk.JsonSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.ResponseCreateParams;
import io.akka.tagging.domain.HospitalizationTag;
import io.akka.tagging.domain.TaggingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.akka.common.KeyUtils.readOpenAiKey;

public class OpenAiClient implements AIClient {

  private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);
  private final OpenAIClient client;

  record OpenAiTaggingResult(
    HospitalizationTag tag,
    int confidencePercentage,
    String reasoning
  ) {
  }

  public OpenAiClient() {
    var key = readOpenAiKey();
    this.client = OpenAIOkHttpClient.builder()
      .apiKey(key)
      .build();
  }

  @Override
  public TaggingResult call(String input) {
    log.trace("Calling OpenAI with input: {}", input);
    ChatModel model = ChatModel.GPT_4O;
    ResponseCreateParams params = ResponseCreateParams.builder()
      .input(input)
      .model(model)
      .build();

    var response = client.responses().create(params);

    var rawTextResponse = response.output().get(0).asMessage().content().get(0).asOutputText().text();
    log.debug("Received response: {}", rawTextResponse);
    var cleanedJson = rawTextResponse
      .replaceAll("(?s)```json\\s*", "")  // remove ```json and optional whitespace
      .replaceAll("(?s)```", "")          // remove ```
      .trim();
    try {
      OpenAiTaggingResult result = JsonSupport.getObjectMapper().readValue(cleanedJson, OpenAiTaggingResult.class);
      return new TaggingResult(
        result.tag,
        result.confidencePercentage,
        result.reasoning,
        model.toString()
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error when parsing response" + rawTextResponse, e);
    }
  }
}
