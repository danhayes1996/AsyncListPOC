package com.drh.poc.asyncList.controller;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

  private static final String[] strings =
      IntStream.range(0, 100).mapToObj(String::valueOf).toArray(String[]::new);

  @GetMapping("/test")
  public String test() {
    //create requests
    CompletableFuture[] futures =
        Arrays.asList(strings).stream()
            .map(s -> CompletableFuture.supplyAsync(() -> getString(s)))
            .toArray(CompletableFuture[]::new);

    //add termimate on failure functionallity to fuctures
    allOfTerminateOnFailure(futures);

    //get results, throw exceptions if they were any.
    return (String) Arrays.stream(futures).map(f -> {
      try {
        return f.get();
      } catch (ExecutionException | InterruptedException e) {
        //get exception thrown in async call
        throw (RuntimeException) e.getCause();
      } catch (CancellationException ce) {
        //ignore cancellation exceptions (caused by CompleteableFuture.cancel())
      }
      return null;
    }).reduce((prev, next) ->  prev + " " + next).get();
  }

  public static void allOfTerminateOnFailure(CompletableFuture<?>... futures) {
    CompletableFuture<?> failure = new CompletableFuture();
    for (CompletableFuture<?> f : futures) {
      f.exceptionally(ex -> {
        failure.completeExceptionally(ex);
        return null;
      });
    }
    failure.exceptionally(ex -> {
      System.out.println("exception thrown: " + ex.getMessage());
      System.out.println("CANCELLING ALL CALLS");
      Arrays.stream(futures).forEach(f -> {
        f.cancel(false);
      });
      return null;
    });
  }

  @Async
  private String getString(String num) {
    try {
      Thread.sleep(new Random().nextInt(200));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("STARTED DONE " + num);
      throw new RuntimeException("Test exception");
//    return num + " done";
  }
}
