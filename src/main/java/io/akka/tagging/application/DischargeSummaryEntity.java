package io.akka.tagging.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.keyvalueentity.KeyValueEntity;
import io.akka.tagging.domain.DischargeSummary;
import io.akka.tagging.domain.HospitalizationTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static akka.Done.done;

@ComponentId("discharge-summary")
public class DischargeSummaryEntity extends KeyValueEntity<DischargeSummary> {

  private static final Logger logger = LoggerFactory.getLogger(DischargeSummaryEntity.class);

  public record CreateCommand(String description, Instant createdAt, HospitalizationTag tag) {
  }

  public Effect<Done> create(CreateCommand command) {
    if (currentState() != null) {
      logger.info("Discharge summary already exists with id '{}'", commandContext().entityId());
      return effects().reply(done());
    }

    var dischargeSummary = new DischargeSummary(
      Integer.parseInt(commandContext().entityId()),
      Instant.now(),
      command.description,
      command.tag
    );

    logger.info("Creating discharge summary with id '{}'", commandContext().entityId());
    return effects()
      .updateState(dischargeSummary)
      .thenReply(done());
  }

  public ReadOnlyEffect<DischargeSummary> get() {
    if (currentState() == null) {
      return effects().error("No discharge summary found for id '" + commandContext().entityId() + "'");
    } else {
      return effects().reply(currentState());
    }
  }

  public Effect<Done> delete() {
    if (currentState() == null) {
      return effects().error("No discharge summary found for id '" + commandContext().entityId() + "'");
    } else {
      logger.info("Deleting discharge summary with id '{}'", commandContext().entityId());
      return effects()
        .deleteEntity()
        .thenReply(done());
    }
  }
}