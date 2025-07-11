package io.akka.tagging.application;

import akka.javasdk.agent.Agent;
import akka.javasdk.agent.JsonParsingException;
import akka.javasdk.annotations.AgentDescription;
import akka.javasdk.annotations.ComponentId;
import com.typesafe.config.Config;
import io.akka.tagging.domain.HospitalizationTag;
import io.akka.tagging.domain.TaggingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("tagging-agent")
@AgentDescription(name = "Tagging agent", description = "Tag medical discharge summaries with hospitalization tags")
public class TaggingAgent extends Agent {

  public static final String TAGGING_REQUEST_MESSAGE = """
    You are a pediatrician. Below I will give you a patient discharge summary. Your job is to tag the summary with three possible tags:
    - HOSPITALIZATION_REQUIRED (hospitalization was definitely required)
    - HOSPITALIZATION_NOT_REQUIRED (hospitalization was definitely not required)
    - UNCERTAIN (uncertainty about whether hospitalization was necessary)
    - ERROR (provided summary is not valid or you are not able to determine the tag)
    
    please always answer in JSON format with the following structure:
    {
      "tag": "HOSPITALIZATION_REQUIRED",
      "confidencePercentage": 100,
      "reasoning": "The patient was diagnosed with a severe condition that required immediate hospitalization."
    }
    
    ConfidencePercentage should be a number between 0 and 100, and reasoning should be a string explaining why the tag was assigned.
    Remember to return just the raw JSON without any additional text.
    
    Here is the discharge summary:
    
    """;
  private static final Logger log = LoggerFactory.getLogger(TaggingAgent.class);

  record OpenAiTaggingResult(
    HospitalizationTag tag,
    int confidencePercentage,
    String reasoning
  ) {
  }

  public record TagSummary(String systemPrompt, String dischargeSummary) {
  }

  private final Config config;

  public TaggingAgent(Config config) {
    this.config = config;
  }

  public Effect<TaggingResult> run(TagSummary tagSummary) {

    return effects()
      .systemMessage(tagSummary.systemPrompt)
      .userMessage(tagSummary.dischargeSummary)
      .responseAs(OpenAiTaggingResult.class)
      .map(res -> new TaggingResult(res.tag(), res.confidencePercentage(), res.reasoning(), getModelName()))
      .onFailure(throwable -> {
        if (throwable instanceof JsonParsingException jsonParsingException) {
          // Log the raw JSON for debugging purposes
          log.error(jsonParsingException.getMessage() + ", raw JSON: " + jsonParsingException.getRawJson(), throwable);
        }
        throw new RuntimeException(throwable);
      })
      .thenReply();
  }

  private String getModelName() {
    var openAiModel = config.getString("akka.javasdk.agent.openai.model-name");
    if (openAiModel.isBlank()) {
      return "unknown-model";
    } else {
      return openAiModel;
    }
  }
}
