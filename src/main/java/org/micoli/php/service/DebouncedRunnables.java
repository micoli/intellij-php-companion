package org.micoli.php.service;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class DebouncedRunnables {
    private @NotNull final Map<String, DebouncedRunnable> debouncedActions = new HashMap<>();

    public DebouncedRunnable run(@NotNull Runnable task, @NotNull String name, long delayMillis) {
        return run(task, name, delayMillis, null);
    }

    public DebouncedRunnable run(@NotNull Runnable task, @NotNull String name, long delayMillis, Runnable callback) {
        DebouncedRunnable debouncedRunnable =
                debouncedActions.computeIfAbsent(name, k -> new DebouncedRunnable(task, delayMillis, callback));
        debouncedRunnable.run();
        return debouncedRunnable;
    }

    public void reset() {
        for (DebouncedRunnable debouncedRunnable : debouncedActions.values()) {
            debouncedRunnable.close();
        }
        debouncedActions.clear();
    }
}
