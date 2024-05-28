package org.koekepan.VAST.Packet;

import java.util.concurrent.*;

public class ExecutorServiceSingleton {
    private static ScheduledExecutorService executorService;

    private ExecutorServiceSingleton() {}

    public static synchronized ScheduledExecutorService getInstance() {
        if (executorService == null) {
            executorService = Executors.newScheduledThreadPool((int) Runtime.getRuntime().availableProcessors() * 10);

            ScheduledThreadPoolExecutor scheduledExecutorService = (ScheduledThreadPoolExecutor) executorService;

            Runnable monitor = () -> {
                while (!executorService.isTerminated()) {
                    System.out.println("Active threads: " + scheduledExecutorService.getActiveCount());
                    System.out.println("Queued tasks: " + scheduledExecutorService.getQueue().size());
                    try {
                        Thread.sleep(1000); // Sleep for a second before printing again
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };

            // Start the monitoring thread
            Thread monitorThread = new Thread(monitor);
            monitorThread.start();

        }
        return executorService;
    }

    public static void executeWithTimeout(Runnable task, long timeout, TimeUnit unit) {
        ScheduledExecutorService executor = getInstance();
        Future<?> future = executor.submit(task);

        // Schedule a task to cancel the running task if it exceeds the timeout
        executor.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true); // This interrupts the thread executing the task
            }
        }, timeout, unit);
    }
}