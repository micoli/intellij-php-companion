package org.micoli.php.service

import java.lang.AutoCloseable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class DebouncedRunnable(
    private val task: Runnable,
    val delayMillis: Long,
    private val callback: Runnable?
) : AutoCloseable {
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var scheduledTask: ScheduledFuture<*>? = null

    @Synchronized
    fun run() {
        if (scheduledTask != null) {
            scheduledTask!!.cancel(false)
        }

        scheduledTask = schedule(task, delayMillis)
    }

    fun schedule(runnable: Runnable, delayMillis: Long): ScheduledFuture<*> {
        return scheduler.schedule(
            {
                try {
                    runnable.run()
                } finally {
                    callback?.run()
                }
            },
            delayMillis,
            TimeUnit.MILLISECONDS)
    }

    override fun close() {
        if (scheduledTask != null) {
            scheduledTask!!.cancel(true)
        }
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (_: InterruptedException) {
            scheduler.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }

    @Synchronized
    fun executeNow() {
        if (scheduledTask != null) {
            scheduledTask!!.cancel(false)
            scheduledTask = null
        }

        task.run()
    }
}
