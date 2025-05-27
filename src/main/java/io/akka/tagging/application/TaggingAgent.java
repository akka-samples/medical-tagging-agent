package io.akka.tagging.application;

import akka.javasdk.agent.Agent;
import akka.javasdk.agent.JsonParsingException;
import akka.javasdk.agent.ModelProvider;
import akka.javasdk.annotations.AgentDescription;
import akka.javasdk.annotations.ComponentId;
import io.akka.tagging.domain.HospitalizationTag;
import io.akka.tagging.domain.TaggingResult;

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

  record OpenAiTaggingResult(
    HospitalizationTag tag,
    int confidencePercentage,
    String reasoning
  ) {
  }

  public record TagSummary(String systemPrompt, String dischargeSummary) {
  }

  private final ModelProvider modelProvider;

  public TaggingAgent(ModelProvider modelProvider) {
    this.modelProvider = modelProvider;
  }

  public Effect<TaggingResult> run(TagSummary tagSummary) {

    return effects()
      .model(modelProvider)
      .systemMessage(tagSummary.systemPrompt)
      .userMessage(tagSummary.dischargeSummary)
      .responseAs(OpenAiTaggingResult.class)
      .map(res -> new TaggingResult(res.tag(), res.confidencePercentage(), res.reasoning(), getModelName(modelProvider)))
      .onFailure(throwable -> {
        if (throwable instanceof JsonParsingException jsonParsingException) {
          // Log the raw JSON for debugging purposes
          System.err.println("Raw JSON: " + jsonParsingException.getRawJson());
          throw new RuntimeException(throwable);
        } else {
          throw new RuntimeException(throwable);
        }
      })
      .thenReply();
  }

  private String getModelName(ModelProvider modelProvider) {
    if (modelProvider instanceof ModelProvider.OpenAi openAi) {
      return openAi.modelName();
    } else {
      return "unknown-model";
    }
  }
}
