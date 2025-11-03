package org.micoli.php.symfony.profiler.htmlParsers

import com.intellij.openapi.diagnostic.Logger
import java.net.URLDecoder
import java.text.ParseException
import java.util.regex.Pattern
import org.jaxen.jdom.JDOMXPath
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Parent
import org.jdom2.filter.Filters
import org.jdom2.output.XMLOutputter
import org.jdom2.xpath.XPathExpression
import org.jdom2.xpath.XPathFactory
import org.jsoup.Jsoup

class FileLocation(
    val file: String,
    val line: Int?,
)

abstract class HtmlParser {
    val outputter = XMLOutputter()
    private val xpathFactory = XPathFactory.instance()

    abstract fun getTargetClass(): Any

    abstract fun parse(document: Document): Any

    protected fun cleanupDump(dump: String): String {
        return dump.replace(Regex("#\\d+$", RegexOption.MULTILINE), "")
    }

    @Throws(ParseException::class)
    fun parseDuration(durationString: String?): Double? {
        val p: Pattern = Pattern.compile("(\\d+.\\d+)\\sms")
        val sanitizedDurationString = (durationString?.trimIndent() ?: "").replace(" ", " ")
        val m = p.matcher(sanitizedDurationString)

        if (m.find() && m.groupCount() == 1) {
            return m.group(1).toDouble()
        } else {
            LOGGER.warn("Cannot parse duration $durationString")
            return null
        }
    }

    fun parseFileUri(uri: String): FileLocation? {
        return when {
            uri.startsWith("phpstorm://") -> parsePhpStormUri(uri)
            uri.startsWith("file://") -> parseFileSlashUri(uri)
            else -> null
        }
    }

    protected fun parsePhpStormUri(uri: String): FileLocation? {
        return try {
            val query = uri.substringAfter('?')
            val params =
                query.split('&').associate { param ->
                    val (key, value) = param.split('=', limit = 2)
                    key to URLDecoder.decode(value, "UTF-8")
                }

            return FileLocation(
                params["file"] ?: return null, params["line"]?.toIntOrNull() ?: return null)
        } catch (_: Exception) {
            null
        }
    }

    protected fun parseFileSlashUri(uri: String): FileLocation? {
        return try {
            val withoutScheme = uri.substringAfter("file://")

            val (path, lineStr) =
                if ('#' in withoutScheme) {
                    val parts = withoutScheme.split('#', limit = 2)
                    parts[0] to parts[1]
                } else {
                    withoutScheme to "1"
                }

            return FileLocation(
                URLDecoder.decode(path, "UTF-8"),
                lineStr.replaceFirst("L", "").toIntOrNull() ?: return null)
        } catch (_: Exception) {
            null
        }
    }

    protected fun compileXPath(expression: String): XPathExpression<Element> {
        return xpathFactory.compile(expression, Filters.element())
    }

    protected fun xPathHTMLText(xpath: XPathExpression<Element>, context: Any?): String {
        val element = xpath.evaluateFirst(context) ?: return ""
        return Jsoup.parse(outputter.outputString(element) ?: "").text()
    }

    protected fun xPathHTML(xpath: XPathExpression<Element>, context: Any?): String {
        val element = xpath.evaluateFirst(context)
        return if (element == null) "" else outputter.outputString(element)
    }

    protected fun xPathElements(xpath: XPathExpression<Element>, context: Any): List<Element> {
        return xpath.evaluate(context)
    }

    protected fun xPathFirstElement(xpath: XPathExpression<Element>, context: Any): Element? {
        return xpath.evaluateFirst(context)
    }

    protected fun <T> measureTime(label: String, block: () -> T): T {
        val startTime = System.nanoTime()
        val result = block()
        val duration = (System.nanoTime() - startTime) / 1_000_000.0
        println("$label: ${String.format("%.2f", duration)}ms")
        return result
    }

    protected fun xPathHTMLText(xPath: JDOMXPath, logRow: Any?): String {
        val element = xPath.selectNodes(logRow).firstOrNull()?.let { (it as Element) }
        if (element == null) return ""
        return Jsoup.parse(outputter.outputString(element) ?: "").text()
    }

    protected fun xPathHTML(xPath: JDOMXPath, logRow: Any?): String {
        val element = xPath.selectNodes(logRow).firstOrNull()?.let { (it as Element) }
        return if (element == null) "" else outputter.outputString(element)
    }

    protected fun xPathElements(xpath: JDOMXPath, queryRow: Parent): List<Element> {
        return xpath.selectNodes(queryRow).filterIsInstance<Element>()
    }

    protected fun xPathFirstElements(xpath: JDOMXPath, queryRow: Element): Element? {
        return xpath.selectNodes(queryRow).filterIsInstance<Element>().firstOrNull()
    }

    companion object {
        private val LOGGER = Logger.getInstance(HtmlParser::class.java.getSimpleName())
    }
}
