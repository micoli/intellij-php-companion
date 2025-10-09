package org.micoli.php.symfony.profiler.parsers

import org.jaxen.jdom.JDOMXPath
import org.jdom.Document

class RequestData(val controller: String, val route: String)

class RequestParser : Parser() {
    override fun getPage(): String = "request"

    override fun parse(document: Document): RequestData {
        val xpathController =
            JDOMXPath(
                "//h3[text()='Request Attributes']/following-sibling::div[1]/table/tbody/tr/th[text()='_controller']/following-sibling::td")
        val xpathRoute =
            JDOMXPath(
                "//h3[text()='Request Attributes']/following-sibling::div[1]/table/tbody/tr/th[text()='_route']/following-sibling::td")

        return RequestData(
            xPathHTMLText(xpathController, document).trim { it <= '\"' },
            xPathHTMLText(xpathRoute, document).trim { it <= '\"' })
    }
}
