package io.akka.tagging.api;

import akka.Done;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import io.akka.tagging.application.DischargeSummaryView;
import io.akka.tagging.application.DischargeSummaryView.DischargeSummaryIds;
import io.akka.tagging.application.TaggingView;
import io.akka.tagging.application.TaggingView.TaggingEntries;
import io.akka.tagging.application.TaggingView.TaggingEntry;
import io.akka.tagging.application.TaggingWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static io.akka.ai.application.AiTaggingService.TAGGING_REQUEST_MESSAGE;

@HttpEndpoint
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class TaggingEndpoint {

  private static final Logger log = LoggerFactory.getLogger(TaggingEndpoint.class);


  private final ComponentClient componentClient;

  public TaggingEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  public record StartTaggingRequest(String prompt) {
  }

  @Get("/taggings")
  public CompletionStage<List<TaggingEntry>> getAllSummaries() {
    return componentClient
      .forView()
      .method(TaggingView::getAll)
      .invokeAsync()
      .thenApply(TaggingEntries::entries);
  }

  @Get("/taggings/{id}")
  public CompletionStage<TaggingEntry> getTagging(String id) {
    return componentClient
      .forView()
      .method(TaggingView::getById)
      .invokeAsync(id);
  }

  @Get("/taggings/last-prompt")
  public CompletionStage<String> getLastPrompt() {
    return componentClient
      .forView()
      .method(TaggingView::getLatestTagging)
      .invokeAsync()
      .thenApply(taggingEntry ->
        taggingEntry.map(TaggingEntry::prompt).orElse(TAGGING_REQUEST_MESSAGE)
      );
  }

  @Post("/taggings")
  public CompletionStage<Done> startTagging(StartTaggingRequest startTagging) {
    return getSummaryId()
      .thenCompose(ids -> startTaggingWorkflow(startTagging, ids));
  }

  private CompletionStage<Done> startTaggingWorkflow(StartTaggingRequest startTagging, List<Integer> dischargeSummaryIds) {
    return componentClient.forView()
      .method(TaggingView::getCount)
      .invokeAsync()
      .thenCompose(countResult -> {
        long workflowId = countResult.count() + 1;
        log.info("Starting tagging workflow with id {}", workflowId);
        return componentClient.forWorkflow(String.valueOf(workflowId))
          .method(TaggingWorkflow::start)
          .invokeAsync(new TaggingWorkflow.StartTagging(startTagging.prompt, dischargeSummaryIds));
      });
  }

  private CompletionStage<List<Integer>> getSummaryId() {
    return componentClient
      .forView()
      .method(DischargeSummaryView::getAllIds)
      .invokeAsync()
      .thenApply(DischargeSummaryIds::ids);
  }
}
