package org.micoli.php.symfony.profiler.htmlParsers

import org.jdom2.Document
import org.micoli.php.symfony.profiler.models.DBData
import org.micoli.php.symfony.profiler.models.DBQuery
import org.micoli.php.symfony.profiler.models.DBStats

class DbHtmlParser : HtmlParser() {
    override fun getTargetClass(): Any {
        return DBData::class.java
    }

    override fun parse(document: Document): DBData {
        val queries = mutableListOf<DBQuery>()
        val entities = mutableListOf<String>()

        val xpathQueryRows =
            compileXPath(
                "//table[contains(@class,'queries-table')]//tbody/tr[starts-with(@id,'query')]")
        val xpathQuerySql = compileXPath(".//pre[contains(@class,'highlight highlight-sql')]")
        val xpathQueryRunnable =
            compileXPath(
                ".//div[starts-with(@id,'original-query') and contains(@class,'sql-runnable')]/pre[contains(@class,'highlight highlight-sql')]")
        val xpathBacktraceRows = compileXPath(".//div[starts-with(@id,'backtrace')]/table/tbody/tr")
        val xpathBacktraceLink = compileXPath(".//a")

        val queryRows = xPathElements(xpathQueryRows, document)

        for (queryRow in queryRows) {
            val cells = queryRow.getChildren("td")
            if (cells.isEmpty()) continue

            val backtracesList = mutableListOf<FileLocation>()

            for (backtrace in xPathElements(xpathBacktraceRows, cells[2])) {
                val backtraceCells = backtrace.getChildren("td")
                if (backtraceCells.size > 1) {
                    val uri =
                        xPathFirstElement(xpathBacktraceLink, backtraceCells[1])
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

        val xpathEntities = compileXPath("//div[h3='Entities Mapping']//tbody//tr")
        val xpathEntityCell = compileXPath(".//td[1]")

        for (row in xPathElements(xpathEntities, document)) {
            entities.add(xPathHTMLText(xpathEntityCell, row))
        }

        val xpathQueryCount =
            compileXPath(
                "//div[@class='metrics']/div/div[@class='metric' and span='Database Queries']/span[@class='value']")
        val xpathStatements =
            compileXPath(
                "//div[@class='metrics']/div/div[@class='metric' and span='Different statements']/span[@class='value']")
        val xpathQueryTime =
            compileXPath(
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
