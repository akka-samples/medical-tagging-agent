package io.akka.tagging.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.client.ComponentClient;
import akka.stream.Materializer;
import io.akka.tagging.application.DischargeSummaryEntity;
import io.akka.tagging.application.DischargeSummaryView;
import io.akka.tagging.domain.DischargeSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


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
  public DischargeSummary get(String id) {
    return componentClient
      .forKeyValueEntity(id)
      .method(DischargeSummaryEntity::get)
      .invoke();
  }

  @Get("/summaries")
  public List<DischargeSummary> getAll() {
    var dischargeSummaries = componentClient
      .forView()
      .method(DischargeSummaryView::getAll)
      .invoke();
    return dischargeSummaries.summaries();
  }
}