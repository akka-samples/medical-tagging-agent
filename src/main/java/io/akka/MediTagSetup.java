package io.akka;

import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.timer.TimerScheduler;
import com.typesafe.config.Config;
import io.akka.importer.application.FakeImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.time.Duration.ofSeconds;

@Setup
public class MediTagSetup implements ServiceSetup {

  private static final Logger logger = LoggerFactory.getLogger(MediTagSetup.class);

  private final ComponentClient componentClient;
  private final TimerScheduler timerScheduler;

  public MediTagSetup(Config config, ComponentClient componentClient, TimerScheduler timerScheduler) {
    if (config.getString("akka.javasdk.agent.model-provider").equals("openai")
      && config.getString("akka.javasdk.agent.openai.api-key").isBlank()) {
      throw new IllegalStateException(
        "No API keys found. Make sure you have OPENAI_API_KEY defined as environment variable, or change the model provider configuration in application.conf to use a different LLM.");
    }
    this.componentClient = componentClient;
    this.timerScheduler = timerScheduler;
  }

  @Override
  public void onStartup() {
    timerScheduler.createSingleTimer("import-data",
      ofSeconds(1),
      componentClient.forTimedAction()
        .method(FakeImporter::importData)
        .deferred(20)
    );
  }
}
