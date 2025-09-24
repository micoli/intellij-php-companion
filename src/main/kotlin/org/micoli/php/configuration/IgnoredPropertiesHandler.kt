package org.micoli.php.configuration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler
import kotlin.Any
import kotlin.Boolean

class IgnoredPropertiesHandler(private val ignorableClasses: MutableList<Class<*>>) :
    DeserializationProblemHandler() {
    private val unknownProperties: MutableList<String> = ArrayList()
    private val ignoredProperties: MutableList<String> = ArrayList()

    override fun handleUnknownProperty(
        ctxt: DeserializationContext,
        p: JsonParser,
        deserializer: JsonDeserializer<*>,
        beanOrClass: Any,
        propertyName: String
    ): Boolean {
        val context =
            String.format(
                "%s (%s)",
                buildPropertyPath(ctxt),
                ctxt.parser.currentTokenLocation().offsetDescription())
        if (ignorableClasses.contains(beanOrClass.javaClass)) {
            ignoredProperties.add(context)
        } else {
            unknownProperties.add(context)
        }
        return true
    }

    fun getUnknownProperties(): MutableList<String> = ArrayList(unknownProperties)

    fun getIgnoredProperties(): MutableList<String> = ArrayList(ignoredProperties)

    private fun buildPropertyPath(ctxt: DeserializationContext): String {
        val pathRef = ctxt.parser.parsingContext
        val pathSegments: MutableList<String?> = ArrayList()
        var current = pathRef

        while (current != null) {
            if (current.hasCurrentName()) {
                pathSegments.addFirst(current.currentName)
            } else if (current.inArray()) {
                pathSegments.addFirst("[" + current.currentIndex + "]")
            }
            current = current.parent
        }

        return pathSegments.joinToString(".")
    }
}
