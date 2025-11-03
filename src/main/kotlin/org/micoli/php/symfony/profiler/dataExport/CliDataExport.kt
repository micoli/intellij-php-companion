package org.micoli.php.symfony.profiler.dataExport

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.intellij.openapi.project.Project
import org.micoli.php.runner.PhpRunnerService
import org.micoli.php.symfony.profiler.configuration.SymfonyProfilerConfiguration

class CliDataExport(val project: Project) : AbstractDataExport() {
    override fun <T> loadProfilerDumpPage(
        configuration: SymfonyProfilerConfiguration?,
        targetClass: Class<T>,
        token: String,
        logCallback: (log: String) -> Unit,
        errorCallback: (String) -> Unit,
        callback: (T?) -> Unit
    ) {
        val page = pages[targetClass] ?: return
        logCallback("Start loading")
        val scriptSource =
            (javaClass.getResourceAsStream("/scripts/profiler-dump-cli.php")
                    ?: throw IllegalArgumentException(
                        "Resource not found: scripts/profiler-dump-cli.php"))
                .bufferedReader()
                .use { it.readText() }
                .replace("<?php", "")
        val output =
            PhpRunnerService.getInstance(project)
                .executePhpViaStdin(
                    scriptSource,
                    "",
                    arrayOf(
                        configuration?.profilerProjectPath ?: "",
                        configuration?.profilerPath ?: "",
                        token,
                        page))
        // println(output)
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(
            KotlinModule.Builder()
                .configure(KotlinFeature.KotlinPropertyNameAsImplicitName, true)
                .build())
        objectMapper.registerModule(JavaTimeModule())
        callback(objectMapper.readValue(output, targetClass) as T?)
    }
}
