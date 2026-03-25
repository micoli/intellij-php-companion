package org.micoli.php.service

import java.lang.AutoCloseable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class DebouncedRunnable(
    private val task: Runnable,
    val delayMillis: Long,
    private val callback: Runnable?,
    private val scheduler: ScheduledExecutorService = SHARED_SCHEDULER
) : AutoCloseable {
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
        // Don't shutdown the shared scheduler
    }

    @Synchronized
    fun executeNow() {
        if (scheduledTask != null) {
            scheduledTask!!.cancel(false)
            scheduledTask = null
        }

        task.run()
    }

    companion object {
        val SHARED_SCHEDULER: ScheduledExecutorService =
            Executors.newScheduledThreadPool(2) { runnable ->
                Thread(runnable, "php-companion-debounce").apply { isDaemon = true }
            }
    }
}
