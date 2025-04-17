package io.akka.ai.application;

import akka.stream.Materializer;
import io.akka.tagging.domain.DischargeSummary;
import io.akka.tagging.domain.TaggingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

/**
 * AI-based implementation of the TaggingService.
 * Analyzes discharge summaries to determine if hospitalization was required.
 */
public class AiTaggingService implements TaggingService {

  private final AIClient aiClient;
  private final Materializer materializer;

  private static final Logger logger = LoggerFactory.getLogger(AiTaggingService.class);

  public static final String TAGGING_REQUEST_MESSAGE = """
    Below I will give you a patient discharge summary. Your job is to tag the summary with three possible tags:
    - HOSPITALIZATION_REQUIRED (hospitalization was definitely required)
    - HOSPITALIZATION_NOT_REQUIRED (hospitalization was definitely not required)
    - UNCERTAIN (uncertainty about whether hospitalization was necessary)
    - ERROR (provided summary is not valid or you are not able to determine the tag)
    
    please always answer in JSON format with the following structure:
    {
      "tag": "HOSPITALIZATION_REQUIRED",
      "confidencePercentage": 100,
      "reasoning": "The patient was diagnosed with a severe condition that required immediate hospitalization."
    }
    
    ConfidencePercentage should be a number between 0 and 100, and reasoning should be a string explaining why the tag was assigned.
    
    Here is the discharge summary:
    
    """;

  public static final String TAGGING_PROMPT_PL = """
    Poniżej podam Ci kartę wypisu pacjenta. Twoim zadaniem jest oznaczyć tę kartę jednym z trzech możliwych tagów:
    - HOSPITALIZATION_REQUIRED (hospitalizacja była zdecydowanie konieczna)
    - HOSPITALIZATION_NOT_REQUIRED (hospitalizacja była zdecydowanie niekonieczna)
    - UNCERTAIN (niepewność co do konieczności hospitalizacji)
    - ERROR (podany opis jest błędy, albo nie jesteś w stanie określić tagu)
    
    Proszę, odpowiedz zawsze w formacie JSON o następującej strukturze:
    {
      "tag": "HOSPITALIZATION_REQUIRED",
      "confidencePercentage": 100,
      "reasoning": "Pacjent został zdiagnozowany z poważnym schorzeniem, które wymagało natychmiastowej hospitalizacji."
    }
    
    Pole "confidencePercentage" powinno być liczbą od 0 do 100. 
    Pole "reasoning" powinno być wyjaśnieniem w języku polskim, dlaczego przypisano dany tag.
    Jeśli nie jesteś w stanie określić tagu albo masz problem z problem kartą, użyj tag "ERROR" i powiedz co było nie tak w polu reasoning.
    
    
    Oto karta wypisu:\s
    
    
    """;

  public AiTaggingService(AIClient aiClient, Materializer materializer) {
    this.aiClient = aiClient;
    this.materializer = materializer;
  }

  @Override
  public CompletionStage<TaggingResult> tagDischargeSummary(DischargeSummary dischargeSummary, String prompt) {
    logger.info("AI tagging service analyzing discharge summary: {}", dischargeSummary.id());
    // Call the AI client to analyze the discharge summary, add more details to the message

    var attempts = 1;
    var retryDelay = Duration.ofSeconds(1);
    return aiClient.call(prompt + dischargeSummary.summary());
//    return Patterns.retry(() -> aiClient.call(TAGGING_REQUEST_MESSAGE + dischargeSummary.description()),
//      attempts,
//      retryDelay,
//      materializer.system());
  }
}
