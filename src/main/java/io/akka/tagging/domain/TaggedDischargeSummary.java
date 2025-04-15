package io.akka.tagging.domain;

import java.time.Instant;


public record TaggedDischargeSummary(
  String id,
  String taggingWorkflowId,
  Instant createdAt, //refers to DischargeSummary createdAt, used for consistent sorting
  Instant taggedAt,
  String description,
  HospitalizationTag expertTag,
  HospitalizationTag aiTag,
  int confidencePercentage,
  String reasoning,
  String aiModel
) {
}