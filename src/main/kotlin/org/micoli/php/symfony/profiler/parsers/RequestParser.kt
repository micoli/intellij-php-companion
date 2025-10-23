package org.micoli.php.symfony.profiler.parsers

import org.jdom2.Document

class RequestData(val controller: String, val route: String)

class RequestParser : Parser() {
    override fun getPage(): String = "request"

    override fun parse(document: Document): RequestData {
        val xpathController =
            compileXPath(
                "//h3[text()='Request Attributes']/following-sibling::div[1]/table/tbody/tr/th[text()='_controller']/following-sibling::td")
        val xpathRoute =
            compileXPath(
                "//h3[text()='Request Attributes']/following-sibling::div[1]/table/tbody/tr/th[text()='_route']/following-sibling::td")

        return RequestData(
            xPathHTMLText(xpathController, document).trim { it <= '\"' },
            xPathHTMLText(xpathRoute, document).trim { it <= '\"' })
    }
}
