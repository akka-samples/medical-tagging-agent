package io.akka.tagging.domain;

/**
 * Represents the result of tagging a discharge summary.
 * Contains the assigned tag, confidence percentage, and reasoning.
 */
public record TaggingResult(
  HospitalizationTag tag,
  int confidencePercentage,
  String reasoning,
  String model
) {
  /**
   * Validates that the confidence percentage is within the allowed range.
   */
  public TaggingResult {
    if (confidencePercentage < 0 || confidencePercentage > 100) {
      throw new IllegalArgumentException("Confidence percentage must be between 0 and 100");
    }

    if (tag == null) {
      throw new IllegalArgumentException("Tag cannot be null");
    }

    if (reasoning == null || reasoning.isBlank()) {
      throw new IllegalArgumentException("Reasoning cannot be null or empty");
    }
  }
}