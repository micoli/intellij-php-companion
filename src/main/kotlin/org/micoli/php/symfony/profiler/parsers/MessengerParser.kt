package org.micoli.php.symfony.profiler.parsers

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jdom2.Document
import org.micoli.php.symfony.list.SearchableRecord

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
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(messageName, messageLocation?.file ?: "", busName, message)
    }
}

class MessengerParser : Parser() {
    override fun getPage(): String = "messenger"

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
                    xPathFirstElement(xpathMessageName, table)?.let {
                        parseFileUri(it.getAttributeValue("href"))
                    },
                    eventBus,
                    xPathFirstElement(xpathMessageCaller, table)?.let {
                        parseFileUri(it.getAttributeValue("href"))
                    },
                    xPathHTMLText(xpathMessageContent, table)))
        }

        return MessengerData(dispatches, MessengerStats(dispatches.size))
    }
}
