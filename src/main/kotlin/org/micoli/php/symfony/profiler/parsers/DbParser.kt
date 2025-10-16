package org.micoli.php.symfony.profiler.parsers

import org.jaxen.jdom.JDOMXPath
import org.jdom.Document
import org.jdom.Element

class DBStats(
    val databaseQueriesCount: Int,
    val differentStatmentsCount: Int,
    val queryTime: Double,
)

class DBData(val queries: List<DBQuery>, entities: List<String>, stats: DBStats)

class DBQuery(
    val index: Number,
    val sql: String,
    val runnableSql: String,
    val htmlSql: String,
    val executionMS: Double,
    val backtrace: List<FileLocation>,
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(sql)
    }
}

class DbParser : Parser() {
    override fun getPage(): String = "db"

    override fun parse(document: Document): DBData {
        val queries = mutableListOf<DBQuery>()
        val entities = mutableListOf<String>()

        val xpathQueryRows =
            JDOMXPath(
                "//table[contains(@class,'queries-table')]//tbody/tr[starts-with(@id,'query')]")
        val xpathQuerySql = JDOMXPath(".//pre[contains(@class,'highlight highlight-sql')]")
        val xpathQueryRunnable =
            JDOMXPath(
                ".//div[starts-with(@id,'original-query') and contains(@class,'sql-runnable')]/pre[contains(@class,'highlight highlight-sql')]")
        val xpathBacktraceRows = JDOMXPath(".//div[starts-with(@id,'backtrace')]/table/tbody/tr")
        val xpathBacktraceLink = JDOMXPath(".//a")

        val queryRows = xpathQueryRows.selectNodes(document).filterIsInstance<Element>()

        for (queryRow in queryRows) {
            val cells = queryRow.getChildren("td")
            if (cells.isEmpty()) continue

            val backtracesList = mutableListOf<FileLocation>()

            for (backtrace in xPathElements(xpathBacktraceRows, queryRow)) {
                val backtraceCells = backtrace.getChildren("td")
                if (backtraceCells.size > 1) {
                    val uri =
                        xPathFirstElements(xpathBacktraceLink, backtraceCells[1])
                            ?.getAttributeValue("href") ?: ""
                    val backTrace = parseFileUri(uri)
                    if (backTrace != null) {
                        backtracesList.add(backTrace)
                    }
                }
            }

            queries.add(
                DBQuery(
                    cells[0].text.toIntOrNull() ?: 0,
                    xPathHTMLText(xpathQuerySql, queryRow),
                    xPathHTMLText(xpathQueryRunnable, queryRow),
                    xPathHTML(xpathQuerySql, queryRow),
                    parseDuration(cells[1].text) ?: -1.0,
                    backtracesList))
        }

        val xpathEntities = JDOMXPath("//div[h3='Entities Mapping']//tbody//tr")
        val xpathEntityCell = JDOMXPath(".//td[1]")

        for (row in xPathElements(xpathEntities, document)) {
            entities.add(xPathHTMLText(xpathEntityCell, row))
        }

        val xpathQueryCount =
            JDOMXPath(
                "//div[@class='metrics']/div/div[@class='metric' and span='Database Queries']/span[@class='value']")
        val xpathStatements =
            JDOMXPath(
                "//div[@class='metrics']/div/div[@class='metric' and span='Different statements']/span[@class='value']")
        val xpathQueryTime =
            JDOMXPath(
                "//div[@class='metrics']/div/div[@class='metric' and span='Query time']/span[@class='value']")

        return DBData(
            queries,
            entities,
            DBStats(
                xPathHTMLText(xpathQueryCount, document).toIntOrNull() ?: 0,
                xPathHTMLText(xpathStatements, document).toIntOrNull() ?: 0,
                parseDuration(xPathHTMLText(xpathQueryTime, document)) ?: -1.0))
    }
}
