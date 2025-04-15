package io.akka.importer.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.timedaction.TimedAction;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import io.akka.tagging.application.DischargeSummaryEntity;
import io.akka.tagging.domain.HospitalizationTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.stream.IntStream;

import static akka.Done.done;
import static io.akka.tagging.domain.HospitalizationTag.getRandomTag;


@ComponentId("fake-importer")
public class FakeImporter extends TimedAction {

  private static final Logger log = LoggerFactory.getLogger(FakeImporter.class);
  private final Materializer materializer;
  private final ComponentClient componentClient;
  private final List<String> exampleSummaries = List.of(
    "Male, 68, came in with high fever, cough, and difficulty breathing. Diagnosed with pneumonia and needed IV antibiotics and oxygen. After 5 days in the hospital, he improved and was discharged with oral antibiotics and follow-up instructions. Hospitalization was necessary.",
    "Female, 54, came to the ER with chest pain and arm tingling. Tests ruled out a heart attack. She was kept overnight for observation. All cardiac tests were normal. Pain resolved by morning. Hospitalization turned out to be unnecessary.",
    "Male, 72, was admitted for shortness of breath and swollen legs due to a heart failure flare-up. Required IV diuretics and oxygen. Over several days, his symptoms improved. Discharged with adjusted meds and strict fluid/diet instructions. Hospitalization was necessary.",
    "Female, 30, had intense flank pain from a kidney stone. Admitted overnight for pain control. Passed the stone on her own the next day and was discharged. Looking back, could have likely been managed at home. Hospitalization not strictly necessary.",
    "Female, 60, underwent a scheduled knee replacement. Surgery went well, and she was in the hospital for 3 days for pain control and physical therapy. She was discharged with rehab instructions. Hospitalization was necessary for post-op recovery.",
    "Male, 17, arrived with a severe asthma attack. He required nebulizer treatments and steroids. Admitted overnight for monitoring. Improved significantly by morning and went home on a steroid taper. Hospitalization was necessary due to severity.",
    "Male, 22, presented with signs of appendicitis. CT confirmed the diagnosis. He underwent laparoscopic appendectomy the same day. Recovered well and was discharged in two days. Hospitalization was absolutely necessary.",
    "Female, 50, experienced a complex migraine with stroke-like symptoms. Admitted overnight while a stroke was ruled out. All tests normal. Headache resolved with treatment. Discharged the next day. Hospitalization ended up being unnecessary.",
    "Male, 19, was admitted in diabetic ketoacidosis due to missed insulin doses. Treated in ICU with IV insulin and fluids. Stabilized and transitioned back to regular insulin injections. Hospitalization was critical and life-saving.",
    "Female, 4, came in dehydrated from vomiting and diarrhea. Given IV fluids and anti-nausea meds. Improved quickly and was discharged the next day. Hospitalization was precautionary and not strictly required."
  );
  private final Random random;

  public FakeImporter(Materializer materializer, ComponentClient componentClient) {
    this.materializer = materializer;
    this.componentClient = componentClient;
    this.random = new Random();
  }

  public Effect importData(int howMany) {
    return effects().asyncDone(
      Source.from(IntStream.rangeClosed(1, howMany).boxed().toList())
        .mapAsync(1, this::createRandomDischargeSummary)
        .run(materializer.system())
        .thenApply(__ -> {
          log.info("Created {} random discharge summaries", howMany);
          return done();
        })
    );
  }

  private CompletionStage<Done> createRandomDischargeSummary(int id) {

    var createCommand = new DischargeSummaryEntity.CreateCommand(
      exampleSummaries.get(id % exampleSummaries.size()),
      Instant.now(),
      getRandomTag());

    return componentClient.forKeyValueEntity(String.valueOf(id))
      .method(DischargeSummaryEntity::create)
      .invokeAsync(createCommand);
  }


}
