package org.micoli.php.service

open class DebouncedRunnables {
    private val debouncedActions: MutableMap<String, DebouncedRunnable> = HashMap()

    fun run(task: Runnable, name: String, delayMillis: Long): DebouncedRunnable? {
        return run(task, name, delayMillis, null)
    }

    open fun run(
        task: Runnable,
        name: String,
        delayMillis: Long,
        callback: Runnable?
    ): DebouncedRunnable? {
        val debouncedRunnable =
            debouncedActions.computeIfAbsent(name) {
                DebouncedRunnable(task, delayMillis, callback)
            }
        debouncedRunnable.run()
        return debouncedRunnable
    }

    fun reset() {
        for (debouncedRunnable in debouncedActions.values) {
            debouncedRunnable.close()
        }
        debouncedActions.clear()
    }
}
