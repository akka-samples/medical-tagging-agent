package io.akka.common;

public class KeyUtils {

  public static boolean hasOpenAiKey() {
    return readOpenAiKey() != null && !readOpenAiKey().isEmpty();
  }

  public static String readOpenAiKey() {
    return readKey("OPENAI_API_KEY");
  }

  private static String readKey(String key) {
    return System.getenv(key);
  }
}
