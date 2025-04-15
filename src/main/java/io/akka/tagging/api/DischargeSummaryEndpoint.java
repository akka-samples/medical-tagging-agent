package io.akka.tagging.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.client.ComponentClient;
import akka.stream.Materializer;
import io.akka.tagging.application.DischargeSummaryEntity;
import io.akka.tagging.application.DischargeSummaryView;
import io.akka.tagging.application.DischargeSummaryView.DischargeSummaries;
import io.akka.tagging.domain.DischargeSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletionStage;


@HttpEndpoint
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class DischargeSummaryEndpoint {

  private static final Logger logger = LoggerFactory.getLogger(DischargeSummaryEndpoint.class);

  private final ComponentClient componentClient;
  private final Materializer materializer;

  public DischargeSummaryEndpoint(ComponentClient componentClient, Materializer materializer) {
    this.componentClient = componentClient;
    this.materializer = materializer;
  }

  public record UpdateTagRequest(String tag,
                                 String reasoning) {
  }

  @Get("/summaries/{id}")
  public CompletionStage<DischargeSummary> get(String id) {
    return componentClient
      .forKeyValueEntity(id)
      .method(DischargeSummaryEntity::get)
      .invokeAsync();
  }

  @Get("/summaries")
  public CompletionStage<List<DischargeSummary>> getAll() {
    return componentClient
      .forView()
      .method(DischargeSummaryView::getAll)
      .invokeAsync()
      .thenApply(DischargeSummaries::summaries);
  }
}