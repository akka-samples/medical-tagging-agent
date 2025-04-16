package io.akka.ai.application;

import io.akka.tagging.domain.TaggingResult;

import java.util.concurrent.CompletionStage;

/**
 * Interface for AI client to interact with an AI service.
 */
public interface AIClient {
    CompletionStage<TaggingResult> call(String input);
}
