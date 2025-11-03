package org.micoli.php.symfony.profiler.htmlParsers

import org.jdom2.Document
import org.micoli.php.symfony.profiler.models.MessengerData
import org.micoli.php.symfony.profiler.models.MessengerDispatch
import org.micoli.php.symfony.profiler.models.MessengerStats

class MessengerHtmlParser : HtmlParser() {
    override fun getTargetClass(): Any {
        return MessengerData::class.java
    }

    override fun parse(document: Document): MessengerData {
        val xpathMessageTables = compileXPath("//table[@class = 'message-item']")
        val xpathMessageBus = compileXPath(".//tbody/tr/th[text()='Bus']/following-sibling::td[1]")
        val xpathMessageName = compileXPath(".//thead//a")
        val xpathMessageCaller =
            compileXPath(".//tbody/tr/th[text()='Caller']/following-sibling::td[1]/a")
        val xpathMessageContent =
            compileXPath(".//tbody/tr/th[text()='Message']/following-sibling::td[1]")

        val dispatches = mutableListOf<MessengerDispatch>()

        val messageTables = xPathElements(xpathMessageTables, document)

        for (table in messageTables) {
            val eventBus = xPathHTMLText(xpathMessageBus, table)
            if (eventBus == "") continue

            dispatches.add(
                MessengerDispatch(
                    xPathHTMLText(xpathMessageName, table),
                    eventBus,
                    xPathFirstElement(xpathMessageCaller, table)?.let {
                        parseFileUri(it.getAttributeValue("href"))
                    },
                    xPathHTMLText(xpathMessageContent, table)))
        }

        return MessengerData(dispatches, MessengerStats(dispatches.size))
    }
}
