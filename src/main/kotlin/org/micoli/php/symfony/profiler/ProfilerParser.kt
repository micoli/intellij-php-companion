package org.micoli.php.symfony.profiler

import org.jdom2.input.SAXBuilder
import org.jsoup.Jsoup
import org.micoli.php.symfony.profiler.parsers.DBData
import org.micoli.php.symfony.profiler.parsers.DbParser
import org.micoli.php.symfony.profiler.parsers.LoggerData
import org.micoli.php.symfony.profiler.parsers.LoggerParser
import org.micoli.php.symfony.profiler.parsers.MessengerData
import org.micoli.php.symfony.profiler.parsers.MessengerParser
import org.micoli.php.symfony.profiler.parsers.RequestData
import org.micoli.php.symfony.profiler.parsers.RequestParser

class ProfilerParser {

    val saxBuilder = SAXBuilder()
    val parsers =
        mapOf(
            LoggerData::class.java to LoggerParser(),
            DBData::class.java to DbParser(),
            MessengerData::class.java to MessengerParser(),
            RequestData::class.java to RequestParser(),
        )

    fun <T> loadProfilerPage(targetClass: Class<T>, htmlContent: String): T {
        val doc = Jsoup.parse(htmlContent)

        doc.select("script, style, meta, link, noscript").remove()
        doc.outputSettings().apply {
            indentAmount(2)
            prettyPrint(true)
            syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml)
        }
        @Suppress("UNCHECKED_CAST")
        try {
            return parsers[targetClass]?.parse(
                saxBuilder.build(doc.outerHtml().trimIndent().trimStart().byteInputStream())) as T
        } catch (e: Exception) {
            throw Exception("Failed to parse profiler page. ${e.localizedMessage}", e)
        }
    }
}
