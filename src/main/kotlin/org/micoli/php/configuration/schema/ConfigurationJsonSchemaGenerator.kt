package org.micoli.php.configuration.schema

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import com.github.victools.jsonschema.module.jackson.JacksonModule
import com.github.victools.jsonschema.module.jackson.JacksonOption

class ConfigurationJsonSchemaGenerator {
    fun generateSchema(clazz: Class<*>?): String? {
        try {
            val mapper = ObjectMapper()
            mapper.enable(SerializationFeature.INDENT_OUTPUT)

            val schemaNode =
                mapper.valueToTree<ObjectNode>(
                    SchemaGenerator(
                            SchemaGeneratorConfigBuilder(
                                    SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
                                .with(
                                    JacksonModule(
                                        JacksonOption.RESPECT_JSONPROPERTY_ORDER,
                                        JacksonOption.RESPECT_JSONPROPERTY_REQUIRED,
                                        JacksonOption.FLATTENED_ENUMS_FROM_JSONVALUE,
                                    ))
                                .build())
                        .generateSchema(clazz))
            schemaNode.put("additionalProperties", false)

            enrichShemaWithAliases(schemaNode, clazz)

            return mapper.writeValueAsString(schemaNode)
        } catch (e: Exception) {
            throw RuntimeException("Erreur lors de la génération du schéma: " + e.message, e)
        }
    }

    private fun enrichShemaWithAliases(schemaNode: ObjectNode, clazz: Class<*>?) {
        val properties = schemaNode.get("properties") as? ObjectNode ?: return

        clazz?.declaredFields?.forEach { field ->
            val jsonAlias = field.getAnnotation(JsonAlias::class.java) ?: return@forEach
            val propDef = properties.get(field.name)
            if (propDef != null) {
                jsonAlias.value.forEach { alias ->
                    properties.set<ObjectNode>(alias, propDef.deepCopy())
                }
            }
        }
    }
}
