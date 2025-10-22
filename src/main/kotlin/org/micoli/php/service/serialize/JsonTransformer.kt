package org.micoli.php.service.serialize

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode

class JsonTransformer(
    val jsonWalker: ((JsonNode) -> JsonNode)? = null,
    val jsonFilter: ((JsonNode, String?) -> Boolean)? = null,
) {
    val mapper = ObjectMapper()

    fun run(node: JsonNode?): JsonNode? {
        var result = node
        if (jsonWalker != null || jsonFilter != null) {
            result = walk(result)
        }

        return result
    }

    private fun walk(node: JsonNode?): JsonNode? {
        if (node == null) return NullNode.instance

        val transformed = if (jsonWalker == null) node else jsonWalker(node)

        return when {
            transformed.isObject -> {
                val obj = ObjectMapper().createObjectNode()
                transformed.properties().forEach { (key, value) ->
                    val value1 = walk(value)
                    if (jsonFilter == null || jsonFilter(value, key)) {
                        obj.putIfAbsent(key, value1)
                    }
                }
                obj
            }

            transformed.isArray -> {
                val array = ObjectMapper().createArrayNode()
                transformed.forEach { item ->
                    if (jsonFilter == null || jsonFilter(item, null)) {
                        array.add(walk(item))
                    }
                }
                array
            }

            else -> transformed
        }
    }
}
