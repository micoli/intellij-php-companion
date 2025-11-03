package org.micoli.php.symfony.profiler.dataExport

import org.micoli.php.symfony.profiler.configuration.SymfonyProfilerConfiguration
import org.micoli.php.symfony.profiler.models.DBData
import org.micoli.php.symfony.profiler.models.LoggerData
import org.micoli.php.symfony.profiler.models.MessengerData
import org.micoli.php.symfony.profiler.models.RequestData

abstract class AbstractDataExport {
    val pages =
        mapOf(
            LoggerData::class.java to "logger",
            DBData::class.java to "db",
            MessengerData::class.java to "messenger",
            RequestData::class.java to "request",
        )

    abstract fun <T> loadProfilerDumpPage(
        configuration: SymfonyProfilerConfiguration?,
        targetClass: Class<T>,
        token: String,
        logCallback: (log: String) -> Unit,
        errorCallback: (String) -> Unit,
        callback: (T?) -> Unit
    )
}
