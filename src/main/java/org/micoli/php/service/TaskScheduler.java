package org.micoli.php.service;

import com.intellij.util.concurrency.AppExecutorUtil;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class TaskScheduler {
    public static void scheduleLater(Runnable runnable, int delayMillis) {
        AppExecutorUtil.getAppScheduledExecutorService()
                .schedule(() -> SwingUtilities.invokeLater(runnable), delayMillis, TimeUnit.MILLISECONDS);
    }
}
