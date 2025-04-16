package io.akka.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FutureUtils {

  public static <T> CompletableFuture<List<T>> all(List<CompletableFuture<T>> futures) {
    CompletableFuture[] cfs = futures.toArray(new CompletableFuture[futures.size()]);

    return CompletableFuture.allOf(cfs)
      .thenApply(ignored -> futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList())
      );
  }
}
