package org.micoli.php.symfony.profiler.models

import com.fasterxml.jackson.annotation.JsonAlias

class PHPBackTrace(
    val file: String,
    val line: Long,
    val function: String?,
    val type: String?,
    @field:JsonAlias("class") val clazz: String?
)

class PHPDBQuery(
    val sql: String,
    val params: Any,
    val executionMS: Double,
    val backtrace: List<PHPBackTrace>,
    val explainable: Boolean,
    val runnable: Boolean
)

class PHPDBQueries(val queries: Map<String, List<PHPDBQuery>>)

class PHPDBData(val data: PHPDBQueries)

class PHPProfilerData(
    val db: PHPDBData,
    val request: Any,
    val time: Any,
    val memory: Any,
    val validator: Any,
    val form: Any,
    val exception: Any,
    val logger: Any,
    val events: Any,
    val router: Any,
    val cache: Any,
    val translation: Any,
    val security: Any,
    @field:JsonAlias("http_client") val httpClient: Any,
    val dump: Any,
    val mailer: Any,
    val messenger: Any,
    // val dbQueries: DBQueries = db.data
)

class PHPProfilerDump(val token: String, val children: List<String>, val data: PHPProfilerData)
