package org.micoli.php.symfony.profiler.htmlParsers

import java.time.OffsetDateTime
import org.jdom2.Document
import org.jdom2.Element
import org.micoli.php.symfony.profiler.models.Log
import org.micoli.php.symfony.profiler.models.LoggerData

class LoggerHtmlParser : HtmlParser() {
    override fun getTargetClass(): Any {
        return LoggerData::class.java
    }

    override fun parse(document: Document): LoggerData {
        val logs = mutableListOf<Log>()

        val xpathRows =
            compileXPath("//table[@class='logs']//tbody/tr[starts-with(@class,'log-status-')]")
        val xpathTime = compileXPath(".//time")
        val xpathSeverity = compileXPath(".//span[contains(@class,'log-type-badge')]")
        val xpathMessage = compileXPath(".//span[contains(@class,'dump-inline')]")
        val xpathChannel = compileXPath(".//div/span[@class='badge']")
        val xpathContext = compileXPath(".//div/div[contains(@class,'context')]")

        for (logRow in xPathElements(xpathRows, document)) {
            val datetime =
                xPathFirstElement(xpathTime, logRow)?.let {
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
