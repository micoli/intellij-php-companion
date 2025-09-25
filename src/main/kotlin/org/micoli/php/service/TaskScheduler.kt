package org.micoli.php.service

import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

object TaskScheduler {
    fun scheduleLater(runnable: Runnable?, delayMillis: Int) {
        AppExecutorUtil.getAppScheduledExecutorService()
            .schedule(
                { SwingUtilities.invokeLater(runnable) },
                delayMillis.toLong(),
                TimeUnit.MILLISECONDS)
    }
}
