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
                double temp = doBusyWork(finalCounter);
                System.out.printf("[%d] End CompletableFuture %d %f %n", Instant.now().toEpochMilli(), finalCounter, temp);

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
        coreExecutor.shutdownNow(); // Forcibly shutdown the executor. This terminates threads that have called `sleep` but those doing busy work will continue to run.
        coreExecutor.close();
    }

    private static double doBusyWork(int finalCounter)
    {
        double currentMax = 0;
        for (long i = 0; i < finalCounter * 1_000_000_000L; i++) {
            // Do some math work to keep the thread busy
            double x = Math.pow(i, 2);
            currentMax = Math.max(currentMax, x);
        }
        return currentMax;
    }

    private static void sleep(int finalCounter)
    {
        try {
            Thread.sleep(500L * finalCounter);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void someOtherMethod()
    {
        System.out.printf("[%d] Some other method started, main thread is running fine.. %n", Instant.now().toEpochMilli());
    }
}



