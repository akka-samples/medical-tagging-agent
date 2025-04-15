package io.akka.tagging.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.DeleteHandler;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import io.akka.tagging.domain.Tagging;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ComponentId("tagging-view")
public class TaggingView extends View {

  public record TaggingEntry(String id, Instant startedAt, Optional<Instant> finishedAt, String prompt, int progress,
                             int correctness, int howManyTagged) {
  }

  public record TaggingEntries(List<TaggingEntry> entries) {
  }

  @Consume.FromWorkflow(TaggingWorkflow.class)
  public static class DischargeSummaryUpdater extends TableUpdater<TaggingEntry> {

    public Effect<TaggingEntry> onChange(Tagging tagging) {
      var entry = new TaggingEntry(
        tagging.id(),
        tagging.startedAt(),
        tagging.finishedAt(),
        tagging.prompt(),
        tagging.progress(),
        tagging.correctness(),
        tagging.allIdsSize() - tagging.pendingSummaryIds().size()
      );
      return effects().updateRow(entry);
    }

    @DeleteHandler
    public Effect<TaggingEntry> onDelete() {
      return effects().deleteRow();
    }
  }

  @Query("SELECT * AS entries FROM taggings ORDER BY startedAt DESC")
  public QueryEffect<TaggingEntries> getAll() {
    return queryResult();
  }

  public record CountResult(long count) {
  }

  @Query("SELECT count(*) FROM taggings")
  public QueryEffect<CountResult> getCount() {
    return queryResult();
  }

  @Query("SELECT * FROM taggings ORDER BY startedAt DESC LIMIT 1")
  public QueryEffect<Optional<TaggingEntry>> getLatestTagging() {
    return queryResult();
  }

  @Query("SELECT * FROM taggings WHERE id = :id")
  public QueryEffect<TaggingEntry> getById(String id) {
    return queryResult();
  }
}
