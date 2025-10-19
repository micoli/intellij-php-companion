package org.micoli.php.configuration.documentation

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.swagger.v3.oas.annotations.media.Schema
import java.util.TreeMap
import org.micoli.php.configuration.documentation.markdown.Bold
import org.micoli.php.configuration.documentation.markdown.BulletListBuilder
import org.micoli.php.configuration.documentation.markdown.MarkdownBuilder
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

class MarkdownSchemaGenerator {
    fun generateMarkdownDescription(clazz: Class<*>): String {
        val classSchema = clazz.getAnnotation(Schema::class.java)
        if (classSchema != null && !classSchema.description.isEmpty()) {
            return classSchema.description
        }
        return ""
    }

    fun generateMarkdownProperties(clazz: Class<*>): String = getYamlProperties(clazz)

    fun generateMarkdownExample(clazz: Class<*>, exampleRoot: String?): String =
        "```yaml\n" +
            generateYamlExample(exampleRoot, clazz).replace("```".toRegex(), "````") +
            "\n```"

    private fun getYamlProperties(clazz: Class<*>): String {
        val example: Any? = (InstanceGenerator()).get(clazz, false)
        val tableRows: MutableList<List<String>> = ArrayList()
        val classPropertyTraverser = ClassPropertiesDocumentationGenerator()
        val fields =
            classPropertyTraverser
                .getProperties(example, 5)
                .stream()
                .sorted(
                    Comparator.comparing(
                        ClassPropertiesDocumentationGenerator.PropertyInfo::dotNotationPath))
                .toList()
        val propertiesListBuilder = BulletListBuilder()
        for (property in fields) {
            tableRows.add(
                listOf(
                    property.dotNotationPath,
                    if (property.description == null || property.description.isEmpty()) {
                        ""
                    } else {
                        property.description
                    },
                ))
            addYamlProperties(propertiesListBuilder, property)
        }
        return MarkdownBuilder()
            .add {
                table {
                    headers("Property", "Description")
                    rows(tableRows)
                }
            }
            .build() + "\n\n" + propertiesListBuilder.build()
    }

    private fun generateYamlExample(exampleRoot: String?, clazz: Class<*>): String {
        val example = InstanceGenerator().get<Any?>(clazz, true)
        var yamlContent: String = dumpYaml(example)

        // @TODO remove when kotlin migration is finished
        yamlContent =
            yamlContent
                .split("\n".toRegex())
                .filter { !it.contains("isEnabled:") }
                .filter { !it.contains("disabled:") }
                .filter { !it.contains("isDisabled:") }
                .joinToString("\n")

        if (exampleRoot == null || exampleRoot.isEmpty()) {
            return yamlContent
        }

        return String.format("%s:\n%s", exampleRoot, indentYamlLines(yamlContent))
    }

    companion object {
        private fun addYamlProperties(
            propertiesListBuilder: BulletListBuilder,
            property: ClassPropertiesDocumentationGenerator.PropertyInfo
        ): BulletListBuilder {
            val detailListBuilder = BulletListBuilder()
            propertiesListBuilder.item(Bold(property.dotNotationPath))
            if (property.description != null && !property.description.isEmpty()) {
                detailListBuilder.item(property.description)
            }
            if (property.example != null && !property.example.isEmpty()) {
                detailListBuilder.item(
                    String.format(
                        "**Example**: ``` %s ```",
                        property.example.replace("```".toRegex(), "````")))
            }

            if (property.defaultValue != null && !property.defaultValue.isEmpty()) {
                detailListBuilder.item(
                    String.format(
                        "**Default Value**: ``` %s ```",
                        property.defaultValue.replace("```".toRegex(), "````")))
            }
            if (!detailListBuilder.items.isEmpty()) {
                return propertiesListBuilder.addSubList(detailListBuilder)
            }
            return propertiesListBuilder
        }

        private fun dumpYaml(example: Any?): String {
            try {
                val mapper = ObjectMapper()
                mapper.registerModule(
                    KotlinModule.Builder()
                        .configure(KotlinFeature.KotlinPropertyNameAsImplicitName, true)
                        .build())
                val value =
                    mapper.readValue(mapper.writeValueAsString(example), TreeMap::class.java)

                val options = DumperOptions()
                options.isExplicitStart = false
                options.isExplicitEnd = false
                options.isCanonical = false
                options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                return Yaml(options).dump(value)
            } catch (e: JsonProcessingException) {
                throw RuntimeException(e)
            }
        }

        private fun indentYamlLines(yamlContent: String, indentation: String = "  "): String =
            yamlContent
                .split("\n".toRegex())
                .dropLastWhile { it.isEmpty() }
                .stream()
                .map { indentation + it }
                .toList()
                .joinToString("\n")
    }
}
