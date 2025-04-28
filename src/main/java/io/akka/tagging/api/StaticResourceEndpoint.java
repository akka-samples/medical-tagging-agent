package io.akka.tagging.api;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.http.HttpResponses;

@HttpEndpoint
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class StaticResourceEndpoint {

  @Get("/") // <1>
  public HttpResponse index() {
    return HttpResponses.staticResource("index.html"); // <2>
  }

  @Get("/custom-bootstrap.css")
  public HttpResponse css() {
    return HttpResponses.staticResource("custom-bootstrap.css");
  }

  @Get("/discharge-summaries.html")
  public HttpResponse dischargeSummaries() {
    return HttpResponses.staticResource("discharge-summaries.html");
  }

  @Get("/tagged-summaries.html")
  public HttpResponse taggedSummaries() {
    return HttpResponses.staticResource("tagged-summaries.html");
  }
}
