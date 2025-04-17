package io.akka.tagging.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.workflow.Workflow;
import io.akka.ai.application.TaggingService;
import io.akka.common.FutureUtils;
import io.akka.tagging.domain.DischargeSummary;
import io.akka.tagging.domain.HospitalizationTag;
import io.akka.tagging.domain.TaggedDischargeSummary;
import io.akka.tagging.domain.Tagging;
import io.akka.tagging.domain.TaggingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.Done.done;

@ComponentId("tagging-workflow")
public class TaggingWorkflow extends Workflow<Tagging> {

  private static final Logger logger = LoggerFactory.getLogger(TaggingWorkflow.class);
  public static final int AI_CALL_BATCH_SIZE = 10;
  private final TaggingService taggingService;
  private final ComponentClient componentClient;

  public TaggingWorkflow(TaggingService taggingService, ComponentClient componentClient) {
    this.taggingService = taggingService;
    this.componentClient = componentClient;
  }

  public record StartTagging(String prompt, List<Integer> summaryIds) {
  }

  record TaggingBatchResult(List<Integer> ids, int correctTags) {
  }

  @Override
  public WorkflowDef<Tagging> definition() {
    var taggingStep =
      step("tagging")
        .asyncCall(() -> runBatch(AI_CALL_BATCH_SIZE))
        .andThen(TaggingBatchResult.class, this::stopOrContinue)
        .timeout(Duration.ofMinutes(1));

    return workflow()
      .addStep(taggingStep);
  }

  public Effect<Done> start(StartTagging startTagging) {
    if (currentState() != null) {
      logger.info("Tagging already exists with id '{}'", commandContext().workflowId());
      return effects().reply(done());
    }
    return effects()
      .updateState(Tagging.create(commandContext().workflowId(), startTagging.prompt, startTagging.summaryIds))
      .transitionTo("tagging")
      .thenReply(done());
  }

  public Effect<Tagging> get() {
    if (currentState() == null) {
      return effects().error("No tagging found for id '" + commandContext().workflowId() + "'");
    } else {
      return effects().reply(currentState());
    }
  }

  private CompletableFuture<TaggingBatchResult> runBatch(int batchSize) {
    var pendingSummaryIds = currentState().getPendingBatch(batchSize);

    return FutureUtils.all(pendingSummaryIds.stream().map(this::tagSummary).toList())
      .thenApply(taggedSummaries ->
        new TaggingBatchResult(pendingSummaryIds, countCorrect(taggedSummaries)));
  }

  private Effect.TransitionalEffect<Void> stopOrContinue(TaggingBatchResult taggingBatchResult) {
    var updatedJob = currentState().updateProgress(taggingBatchResult.ids, taggingBatchResult.correctTags);
    if (updatedJob.isFinished()) {
      logger.info("Tagging job {} is finished, progress [{}]", currentState().id(), updatedJob.progress());
      return effects()
        .updateState(updatedJob)
        .end();
    } else {
      logger.info("Tagging job {} is not finished yet, progress [{}]", currentState().id(), updatedJob.progress());
      return effects()
        .updateState(updatedJob)
        .transitionTo("tagging");
    }
  }

  private int countCorrect(List<TaggedDischargeSummary> taggedSummaries) {
    return taggedSummaries.stream()
      .mapToInt(taggedSummary -> taggedSummary.expertTag().equals(taggedSummary.aiTag()) ? 1 : 0)
      .sum();
  }

  private CompletableFuture<TaggedDischargeSummary> tagSummary(Integer id) {
    return componentClient.forKeyValueEntity(id.toString())
      .method(DischargeSummaryEntity::get)
      .invokeAsync()
      .thenCompose(summary ->
        taggingService.tagDischargeSummary(summary, currentState().prompt())
          .thenCompose(taggingResult -> {
            logger.info("Tagged summary {} with tag {} (confidence: {}%)",
              summary.id(), taggingResult.tag(), taggingResult.confidencePercentage());

            return createTaggedSummary(summary, taggingResult);
          })
          .thenApply(done -> {
            logger.debug("Completed processing for discharge summary: {}", summary.id());
            return done;
          })
          .exceptionallyCompose(error -> {
            //TODO handle exception
            logger.error("Tagging error for discharge summary: {}", summary.id(), error);
            return createTaggedSummary(summary, errorTaggingResult(error));
          })).toCompletableFuture();
  }

  private static TaggingResult errorTaggingResult(Throwable error) {
    return new TaggingResult(
      HospitalizationTag.ERROR,
      100,
      "Error during tagging: " + error.getMessage(),
      ""
    );
  }

  private CompletionStage<TaggedDischargeSummary> createTaggedSummary(DischargeSummary summary, TaggingResult taggingResult) {
    String taggingWorkflowId = currentState().id();
    String taggedId = taggingWorkflowId + "-" + summary.id();
    TaggedDischargeSummaryEntity.CreateCommand createCommand =
      new TaggedDischargeSummaryEntity.CreateCommand(
        taggingWorkflowId,
        summary.createdAt(),
        Instant.now(),
        summary.summary(),
        summary.tag(),
        taggingResult.tag(),
        taggingResult.confidencePercentage(),
        taggingResult.reasoning(),
        taggingResult.model()
      );
    return componentClient
      .forKeyValueEntity(taggedId)
      .method(TaggedDischargeSummaryEntity::create)
      .invokeAsync(createCommand);
  }
}
