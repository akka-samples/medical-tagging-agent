package io.akka.tagging.api;

import akka.javasdk.agent.PromptTemplate;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import io.akka.tagging.application.DischargeSummaryView;
import io.akka.tagging.application.TaggingView;
import io.akka.tagging.application.TaggingView.TaggingEntry;
import io.akka.tagging.application.TaggingWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.akka.tagging.application.TaggingAgent.TAGGING_REQUEST_MESSAGE;

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
  public List<TaggingEntry> getAllSummaries() {
    var taggingEntries = componentClient
      .forView()
      .method(TaggingView::getAll)
      .invoke();
    return taggingEntries.entries();
  }

  @Get("/taggings/{id}")
  public TaggingEntry getTagging(String id) {
    return componentClient
      .forView()
      .method(TaggingView::getById)
      .invoke(id);
  }

  @Get("/taggings/last-prompt")
  public String getLastPrompt() {
    var taggingEntry = componentClient
      .forView()
      .method(TaggingView::getLatestTagging)
      .invoke();

    return taggingEntry.map(TaggingEntry::prompt).orElse(TAGGING_REQUEST_MESSAGE);
  }

  @Post("/taggings")
  public void startTagging(StartTaggingRequest startTagging) {
    var ids = getSummaryId();
    startTaggingWorkflow(startTagging, ids);
  }

  private void startTaggingWorkflow(StartTaggingRequest startTagging, List<Integer> dischargeSummaryIds) {
    var countResult = componentClient.forView()
      .method(TaggingView::getCount)
      .invoke();

    //keep the history of prompts, this is redundant as the workflow state keeps the prompt, but useful for debugging
    componentClient.forEventSourcedEntity("tagging-prompt")
      .method(PromptTemplate::update)
      .invoke(startTagging.prompt());

    long workflowId = countResult.count() + 1;
    log.info("Starting tagging workflow with id {}", workflowId);
    componentClient.forWorkflow(String.valueOf(workflowId))
      .method(TaggingWorkflow::start)
      .invoke(new TaggingWorkflow.StartTagging(startTagging.prompt, dischargeSummaryIds));
  }

  private List<Integer> getSummaryId() {
    var summaryIds = componentClient
      .forView()
      .method(DischargeSummaryView::getAllIds)
      .invoke();
    return summaryIds.ids();
  }
}
