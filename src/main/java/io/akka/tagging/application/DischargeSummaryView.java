package io.akka.tagging.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.DeleteHandler;
import akka.javasdk.annotations.Query;
import akka.javasdk.annotations.Table;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import io.akka.tagging.domain.DischargeSummary;

import java.util.List;

/**
 * A View to query discharge summaries
 */
@ComponentId("discharge-summaries-view")
public class DischargeSummaryView extends View {


  public record DischargeSummaries(List<DischargeSummary> summaries) {
  }

  public record DischargeSummaryIds(List<Integer> ids) {
  }


  @Consume.FromKeyValueEntity(DischargeSummaryEntity.class)
  @Table("discharge_summaries")
  public static class DischargeSummaryUpdater extends TableUpdater<DischargeSummary> {

    public Effect<DischargeSummary> onChange(DischargeSummary summary) {
      return effects().updateRow(summary);
    }

    @DeleteHandler
    public Effect<DischargeSummary> onDelete() {
      return effects().deleteRow();
    }
  }

  @Query("select * as summaries from discharge_summaries order by id desc")
  public QueryEffect<DischargeSummaries> getAll() {
    return queryResult();
  }

  @Query("select id as ids from discharge_summaries order by id asc")
  public QueryEffect<DischargeSummaryIds> getAllIds() {
    return queryResult();
  }
}