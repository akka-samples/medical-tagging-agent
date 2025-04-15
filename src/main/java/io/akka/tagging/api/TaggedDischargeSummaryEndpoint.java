package io.akka.tagging.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.client.ComponentClient;
import io.akka.tagging.application.TaggedDischargeSummaryView;
import io.akka.tagging.domain.TaggedDischargeSummary;

import java.util.List;
import java.util.concurrent.CompletionStage;

@HttpEndpoint
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class TaggedDischargeSummaryEndpoint {

  private final ComponentClient componentClient;

  public TaggedDischargeSummaryEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Get("/tagged-summaries/{taggingId}")
  public CompletionStage<List<TaggedDischargeSummary>> get(String taggingId) {
    return componentClient
      .forView()
      .method(TaggedDischargeSummaryView::getAllForTagging)
      .invokeAsync(taggingId)
      .thenApply(TaggedDischargeSummaryView.TaggedSummaries::summaries);
  }

  @Get("/tagged-summaries")
  public CompletionStage<List<TaggedDischargeSummary>> getAll() {
    return componentClient
      .forView()
      .method(TaggedDischargeSummaryView::getAll)
      .invokeAsync()
      .thenApply(TaggedDischargeSummaryView.TaggedSummaries::summaries);
  }
}
