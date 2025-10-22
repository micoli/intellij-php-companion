package org.micoli.php.service.attributes

import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpAttribute.PhpAttributeArgument
import java.util.function.Function
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.collections.MutableCollection
import kotlin.collections.MutableList
import kotlin.collections.MutableMap

class AttributeMapping(
    private val valueExtractors: LinkedHashMap<String, Function<PhpAttributeArgument, String>>
) : Cloneable {
    private val positionalParamsOrder: MutableList<String> =
        extractConstructorParameters(valueExtractors)
    private var positionalIndex = 0

    public override fun clone(): AttributeMapping {
        try {
            return super.clone() as AttributeMapping
        } catch (e: CloneNotSupportedException) {
            throw AssertionError("Clone as failed", e)
        }
    }

    fun resolveParameterName(argument: PhpAttributeArgument): String {
        var name = argument.name
        if (name.isEmpty() && positionalIndex < positionalParamsOrder.size) {
            name = positionalParamsOrder[positionalIndex++]
        }
        return name
    }

    fun extractValues(attribute: PhpAttribute): MutableMap<String, String?> {
        val extractedValues: MutableMap<String, String?> = HashMap()

        val arguments: MutableCollection<PhpAttributeArgument> = attribute.arguments
        for (argument in arguments) {
            val paramName = resolveParameterName(argument)
            val value = extractValue(paramName, argument)
            extractedValues[paramName] = value
        }
        return extractedValues
    }

    fun extractValue(paramName: String, argument: PhpAttributeArgument): String? {
        val callback = valueExtractors.getOrDefault(paramName, null) ?: return null
        return callback.apply(argument)
    }

    companion object {
        private fun extractConstructorParameters(
            attributeClass: LinkedHashMap<String, Function<PhpAttributeArgument, String>>
        ): MutableList<String> {
            return ArrayList(attributeClass.keys)
        }
    }
}
