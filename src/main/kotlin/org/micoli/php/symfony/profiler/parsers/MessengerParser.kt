package org.micoli.php.symfony.profiler.parsers

import org.jaxen.jdom.JDOMXPath
import org.jdom.Document
import org.jdom.Element

class MessengerStats(
    val messageCount: Int,
)

class MessengerData(val dispatches: List<MessengerDispatch>, val stats: MessengerStats)

class MessengerDispatch(
    val messageName: String,
    val messageLocation: FileLocation?,
    val busName: String,
    val dispatch: FileLocation?,
    val message: String
)

class MessengerParser : Parser() {
    override fun getPage(): String = "messenger"

    override fun parse(document: Document): MessengerData {
        val xpathMessageTables = JDOMXPath("//table[@class = 'message-item']")
        val xpathMessageBus = JDOMXPath(".//tbody/tr/th[text()='Bus']/following-sibling::td[1]")
        val xpathMessageName = JDOMXPath(".//thead//a")
        val xpathMessageCaller =
            JDOMXPath(".//tbody/tr/th[text()='Caller']/following-sibling::td[1]/a")
        val xpathMessageContent =
            JDOMXPath(".//tbody/tr/th[text()='Message']/following-sibling::td[1]")

        val dispatches = mutableListOf<MessengerDispatch>()

        val messageTables = xpathMessageTables.selectNodes(document).filterIsInstance<Element>()

        for (table in messageTables) {
            val eventBus = xPathHTMLText(xpathMessageBus, table)
            if (eventBus == "") continue

            dispatches.add(
                MessengerDispatch(
                    xPathHTMLText(xpathMessageName, table),
                    xPathFirstElements(xpathMessageName, table)?.let {
                        parseFileUri(it.getAttributeValue("href"))
                    },
                    eventBus,
                    xPathFirstElements(xpathMessageCaller, table)?.let {
                        parseFileUri(it.getAttributeValue("href"))
                    },
                    xPathHTMLText(xpathMessageContent, table)))
        }

        return MessengerData(dispatches, MessengerStats(dispatches.size))
    }
}
