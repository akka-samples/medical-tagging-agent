package io.akka.tagging.domain;


import java.util.Random;

/**
 * Enumeration of predefined tags for hospital discharge summaries.
 * This represents the classification options for whether hospitalization
 * was required based on discharge summaries.
 */
public enum HospitalizationTag {
  /**
   * Indicates hospitalization was definitely required.
   */
  HOSPITALIZATION_REQUIRED,

  /**
   * Indicates hospitalization was definitely not required.
   */
  HOSPITALIZATION_NOT_REQUIRED,

  /**
   * Indicates uncertainty about whether hospitalization was necessary.
   */
  UNCERTAIN,

  /**
   * Indicates an error in processing the discharge summary.
   */
  ERROR;

  static public HospitalizationTag getRandomTag() {
    HospitalizationTag[] tags = HospitalizationTag.values();
    return tags[new Random().nextInt(tags.length - 1)]; //without ERROR
  }
}