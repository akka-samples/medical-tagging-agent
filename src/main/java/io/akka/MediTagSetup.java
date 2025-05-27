package io.akka;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.agent.ModelProvider;
import akka.javasdk.annotations.Setup;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.timer.TimerScheduler;
import io.akka.importer.application.FakeImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.time.Duration.ofSeconds;

@Setup
public class MediTagSetup implements ServiceSetup {

  private static final Logger logger = LoggerFactory.getLogger(MediTagSetup.class);

  private final ComponentClient componentClient;
  private final TimerScheduler timerScheduler;

  public MediTagSetup(ComponentClient componentClient, TimerScheduler timerScheduler) {
    this.componentClient = componentClient;
    this.timerScheduler = timerScheduler;
  }

  @Override
  public void onStartup() {
    timerScheduler.createSingleTimer("import-data",
      ofSeconds(1),
      componentClient.forTimedAction()
//        .method(KidAidImporterAction::importData)
//        .deferred()
        .method(FakeImporter::importData)
        .deferred(20)
    );
  }

  @Override
  public DependencyProvider createDependencyProvider() {

    var modelProvider = getModelProvider();

    return new DependencyProvider() {
      @Override
      public <T> T getDependency(Class<T> cls) {
        if (cls.equals(ModelProvider.class)) {
          return (T) modelProvider;
        }
        return null;
      }
    };
  }

  private static ModelProvider getModelProvider() {
    return ModelProvider.openAi()
      .withApiKey(System.getenv("OPENAI_API_KEY"))
      .withModelName("gpt-4o");
  }
}
