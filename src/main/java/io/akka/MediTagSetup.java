package io.akka;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.timer.TimerScheduler;
import akka.stream.Materializer;
import io.akka.ai.application.AiTaggingService;
import io.akka.ai.application.OpenAiClient;
import io.akka.ai.application.TaggingService;
import io.akka.common.KeyUtils;
import io.akka.importer.application.FakeImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.time.Duration.ofSeconds;

@Setup
public class MediTagSetup implements ServiceSetup {

  private static final Logger logger = LoggerFactory.getLogger(MediTagSetup.class);

  private final ComponentClient componentClient;
  private final Materializer materializer;
  private final TimerScheduler timerScheduler;

  public MediTagSetup(ComponentClient componentClient, Materializer materializer, TimerScheduler timerScheduler) {
    if (!KeyUtils.hasValidKeys()) {
      throw new IllegalStateException(
        "No API keys found. When running locally, make sure you have a " + ".env.local file located under " +
          "src/main/resources/ (see src/main/resources/.env.example). When running in production, " +
          "make sure you have OPENAI_API_KEY defined as environment variable.");
    }
    this.componentClient = componentClient;
    this.materializer = materializer;
    this.timerScheduler = timerScheduler;
  }

  @Override
  public void onStartup() {
    timerScheduler.startSingleTimer("import-data",
      ofSeconds(1),
      componentClient.forTimedAction()
//        .method(KidAidImporterAction::importData)
//        .deferred()
        .method(FakeImporter::importData)
        .deferred(50)
    );
  }

  @Override
  public DependencyProvider createDependencyProvider() {

    var taggingService = new AiTaggingService(new OpenAiClient(), materializer);

    return new DependencyProvider() {
      @Override
      public <T> T getDependency(Class<T> cls) {
        if (cls.equals(TaggingService.class)) {
          return (T) taggingService;
        }
        return null;
      }
    };
  }
}
