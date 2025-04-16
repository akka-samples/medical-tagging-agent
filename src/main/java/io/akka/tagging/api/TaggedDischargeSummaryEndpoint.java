package io.akka.tagging.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.client.ComponentClient;
import io.akka.tagging.application.TaggedDischargeSummaryView;
import io.akka.tagging.domain.TaggedDischargeSummary;

import java.util.List;

@HttpEndpoint
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class TaggedDischargeSummaryEndpoint {

  private final ComponentClient componentClient;

  public TaggedDischargeSummaryEndpoint(ComponentClient componentClient) {
    this.componentClient = componentClient;
  }

  @Get("/tagged-summaries/{taggingId}")
  public List<TaggedDischargeSummary> get(String taggingId) {
    var taggedSummaries = componentClient
      .forView()
      .method(TaggedDischargeSummaryView::getAllForTagging)
      .invoke(taggingId);
    return taggedSummaries.summaries();
  }

  @Get("/tagged-summaries")
  public List<TaggedDischargeSummary> getAll() {
    var taggedSummaries = componentClient
      .forView()
      .method(TaggedDischargeSummaryView::getAll)
      .invoke();
    return taggedSummaries.summaries();
  }
}
