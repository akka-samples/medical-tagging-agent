package io.akka.tagging.domain;

import java.time.Instant;

public record DischargeSummary(
  int id,
  Instant createdAt,
  String summary,
  HospitalizationTag tag
) {
}