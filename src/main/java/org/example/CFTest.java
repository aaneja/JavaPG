package org.example;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class CFTest
{
    public static void main(String[] args)
            throws ExecutionException, InterruptedException, TimeoutException
    {
        ExecutorService coreExecutor = newCachedThreadPool();
        CompletableFuture<Void>[] partitionQuickStatCompletableFutures = new CompletableFuture[10];
        for (int counter = 0; counter < 10; counter++) {
            int finalCounter = counter;
            partitionQuickStatCompletableFutures[counter] = supplyAsync(() -> {
                System.out.printf("[%d] Start CompletableFuture %d%n", Instant.now().toEpochMilli(), finalCounter);
                try {
                    Thread.sleep(500L * finalCounter);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("[%d] End CompletableFuture %d%n", Instant.now().toEpochMilli(), finalCounter);

                return null;
            }, coreExecutor);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(partitionQuickStatCompletableFutures);
        Stopwatch sw = Stopwatch.createStarted();
        try {
            allOf.get(3000, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException ex) {
            System.out.printf("[%d] TimeoutException %s", Instant.now().toEpochMilli(), Throwables.getStackTraceAsString(ex));
        }
        finally {
            sw.stop();
            System.out.printf("Time taken for CFs to complete: %d ms%n", sw.elapsed(TimeUnit.MILLISECONDS));
            // coreExecutor.close();
        }

        someOtherMethod();
        coreExecutor.shutdownNow(); // Forcibly shutdown the executor
        coreExecutor.close();
    }

    private static void someOtherMethod()
    {
        System.out.printf("[%d] Some other method%n", Instant.now().toEpochMilli());
    }
}



