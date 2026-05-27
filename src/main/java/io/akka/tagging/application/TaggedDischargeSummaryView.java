package io.akka.tagging.application;

import akka.javasdk.annotations.Component;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import io.akka.tagging.domain.TaggedDischargeSummary;

import java.util.List;

@Component(id = "tagged-discharge-summary-view")
public class TaggedDischargeSummaryView extends View {

  public record TaggedSummaries(List<TaggedDischargeSummary> summaries) {
  }

  @Consume.FromKeyValueEntity(TaggedDischargeSummaryEntity.class)
  public static class TaggedSummaryUpdater extends TableUpdater<TaggedDischargeSummary> {
  }

  @Query("SELECT * AS summaries FROM tagged_discharge_summaries")
  public QueryEffect<TaggedSummaries> getAll() {
    return queryResult();
  }

  @Query("SELECT * AS summaries FROM tagged_discharge_summaries WHERE confidencePercentage <= :confidence")
  public QueryEffect<TaggedSummaries> getTaggedSummariesByMinConfidence(int confidence) {
    return queryResult();
  }

  @Query("SELECT * AS summaries FROM tagged_discharge_summaries WHERE taggingWorkflowId =:taggingId")
  public QueryEffect<TaggedSummaries> getAllForTagging(String taggingId) {
    return queryResult();
  }
}