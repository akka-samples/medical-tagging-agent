package io.akka.tagging.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.keyvalueentity.KeyValueEntity;
import io.akka.tagging.domain.HospitalizationTag;
import io.akka.tagging.domain.TaggedDischargeSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static akka.Done.done;


@ComponentId("tagged-discharge-summary")
public class TaggedDischargeSummaryEntity extends KeyValueEntity<TaggedDischargeSummary> {

  private static final Logger logger = LoggerFactory.getLogger(TaggedDischargeSummaryEntity.class);

  public record CreateCommand(
    String taggingWorkflowId,
    Instant createdAt,
    Instant taggedAt,
    String description,
    HospitalizationTag expertTag,
    HospitalizationTag aiTag,
    int confidencePercentage,
    String reasoning,
    String aiModel
  ) {
  }


  public Effect<TaggedDischargeSummary> create(CreateCommand command) {
    if (currentState() != null) {
      logger.info("Tagged discharge summary already exists with id '{}'", commandContext().entityId());
      return effects().reply(currentState());
    }

    logger.info("Creating tagged discharge summary with id '{}'", commandContext().entityId());

    TaggedDischargeSummary taggedDischargeSummary = new TaggedDischargeSummary(
      commandContext().entityId(),
      command.taggingWorkflowId,
      command.createdAt,
      command.taggedAt,
      command.description,
      command.expertTag,
      command.aiTag,
      command.confidencePercentage,
      command.reasoning,
      command.aiModel
    );
    return effects()
      .updateState(taggedDischargeSummary)
      .thenReply(taggedDischargeSummary);
  }

  /**
   * Retrieves a tagged discharge summary.
   *
   * @return The tagged discharge summary
   */
  public ReadOnlyEffect<TaggedDischargeSummary> get() {
    if (currentState() == null) {
      return effects().error("No tagged discharge summary found for id '" + commandContext().entityId() + "'");
    } else {
      return effects().reply(currentState());
    }
  }

  public Effect<Done> delete() {
    if (currentState() == null) {
      return effects().error("No tagged discharge summary found for id '" + commandContext().entityId() + "'");
    }

    logger.info("Deleting tagged discharge summary with id '{}'", commandContext().entityId());

    return effects()
      .deleteEntity()
      .thenReply(done());
  }
}