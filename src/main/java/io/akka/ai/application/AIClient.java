package io.akka.ai.application;

import io.akka.tagging.domain.TaggingResult;

import java.util.concurrent.CompletionStage;

public interface AIClient {
  TaggingResult call(String input);
}
