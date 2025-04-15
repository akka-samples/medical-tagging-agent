package io.akka.tagging.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record Tagging(String id, Instant startedAt, Optional<Instant> finishedAt, String prompt,
                      List<Integer> pendingSummaryIds,
                      int allIdsSize, int correctTags, String result) { //TODO use AI to calculate results

  public static Tagging create(String id, String prompt, List<Integer> summaryIds) {
    return new Tagging(
      id,
      Instant.now(),
      Optional.empty(),
      prompt,
      summaryIds,
      summaryIds.size(),
      0,
      null
    );
  }

  public boolean isFinished() {
    return finishedAt.isPresent();
  }

  public Tagging updateProgress(List<Integer> taggedIds, int correctTags) {
    pendingSummaryIds.removeAll(taggedIds); //TODO find more effective way

    var finishedAt = pendingSummaryIds.isEmpty() ? Optional.of(Instant.now()) : Optional.<Instant>empty();

    return new Tagging(
      id,
      startedAt,
      finishedAt,
      prompt,
      pendingSummaryIds,
      allIdsSize,
      this.correctTags + correctTags,
      result
    );


  }

  public List<Integer> getPendingBatch(int size) {
    return pendingSummaryIds.subList(0, Math.min(size, pendingSummaryIds.size()));
  }

  public int progress() {
    return 100 - (int) ((double) pendingSummaryIds.size() / allIdsSize * 100.0);
  }

  public int correctness() {
    return (int) ((double) correctTags / allIdsSize * 100.0);
  }
}
