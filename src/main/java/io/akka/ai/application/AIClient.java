package io.akka.ai.application;

import io.akka.tagging.domain.TaggingResult;

/**
 * Interface for AI client to interact with an AI service.
 */
public interface AIClient {
  TaggingResult call(String input);
}
