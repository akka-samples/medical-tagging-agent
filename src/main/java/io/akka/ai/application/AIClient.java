package io.akka.ai.application;

import io.akka.tagging.domain.TaggingResult;

import java.util.concurrent.CompletionStage;

public interface AIClient {
  CompletionStage<TaggingResult> call(String input);
}
