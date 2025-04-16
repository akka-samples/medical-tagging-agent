package io.akka.ai.application;

import io.akka.tagging.domain.DischargeSummary;
import io.akka.tagging.domain.TaggingResult;

public interface TaggingService {

  TaggingResult tagDischargeSummary(DischargeSummary dischargeSummary, String prompt);
}