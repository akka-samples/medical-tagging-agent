package io.akka.ai.application;

import io.akka.tagging.domain.TaggingResult;

import static io.akka.tagging.domain.HospitalizationTag.getRandomTag;

public class FakeAiClient implements AIClient {
  @Override
  public TaggingResult call(String input) {
    return new TaggingResult(getRandomTag(), 100, "Fake AI response", "FakeModel");
  }
}
