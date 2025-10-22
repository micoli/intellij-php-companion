package org.micoli.php.symfony.profiler.parsers

import java.time.OffsetDateTime
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jaxen.jdom.JDOMXPath
import org.jdom.Document
import org.jdom.Element
import org.micoli.php.symfony.list.SearchableRecord

class Log(
    val time: OffsetDateTime,
    val channel: String,
    val severity: String,
    val message: String,
    val context: String,
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(severity, channel, message, context)
    }
}

class LoggerData(val logs: List<Log>)

class LoggerParser : Parser() {
    override fun getPage(): String = "logger"

    override fun parse(document: Document): LoggerData {
        val logs = mutableListOf<Log>()

        val xpathRows =
            JDOMXPath("//table[@class='logs']//tbody/tr[starts-with(@class,'log-status-')]")
        val xpathTime = JDOMXPath(".//time")
        val xpathSeverity = JDOMXPath(".//span[contains(@class,'log-type-badge')]")
        val xpathMessage = JDOMXPath(".//span[contains(@class,'dump-inline')]")
        val xpathChannel = JDOMXPath(".//div/span[@class='badge']")
        val xpathContext = JDOMXPath(".//div/div[contains(@class,'context')]")

        for (logRow in xpathRows.selectNodes(document.rootElement)) {
            val datetime =
                xpathTime.selectNodes(logRow).firstOrNull()?.let {
                    (it as Element).getAttributeValue("datetime")
                } ?: ""
            if (datetime.isEmpty()) {
                continue
            }

            logs.add(
                Log(
                    OffsetDateTime.parse(datetime),
                    xPathHTMLText(xpathChannel, logRow),
                    xPathHTMLText(xpathSeverity, logRow),
                    xPathHTMLText(xpathMessage, logRow),
                    cleanupDump(xPathHTMLText(xpathContext, logRow))))
        }

        return LoggerData(logs)
    }
}
