package org.micoli.php.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class DebouncedRunnable implements AutoCloseable {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private @NotNull final Runnable task;
    private final long delayMillis;
    private final Runnable callback;
    private ScheduledFuture<?> scheduledTask;

    public DebouncedRunnable(@NotNull Runnable task, long delayMillis, Runnable callback) {
        this.task = task;
        this.delayMillis = delayMillis;
        this.callback = callback;
    }

    public synchronized void run() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }

        scheduledTask = schedule(task, delayMillis);
    }

    protected ScheduledFuture<?> schedule(Runnable runnable, long delayMillis) {
        return scheduler.schedule(
                () -> {
                    try {
                        runnable.run();
                    } finally {
                        if (callback != null) {
                            callback.run();
                        }
                    }
                },
                delayMillis,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void executeNow() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }

        task.run();
    }

    public long getDelayMillis() {
        return delayMillis;
    }
}
