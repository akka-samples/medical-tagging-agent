package io.akka.ai.application;

import io.akka.tagging.domain.DischargeSummary;
import io.akka.tagging.domain.TaggingResult;

import java.util.concurrent.CompletionStage;

public interface TaggingService {

  CompletionStage<TaggingResult> tagDischargeSummary(DischargeSummary dischargeSummary, String prompt);
}