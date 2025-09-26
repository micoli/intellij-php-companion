package org.micoli.php.configuration.documentation

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.swagger.v3.oas.annotations.media.Schema
import java.util.Arrays
import java.util.TreeMap
import java.util.stream.Collectors
import net.steppschuh.markdowngenerator.list.UnorderedList
import net.steppschuh.markdowngenerator.table.Table
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
        val tableBuilder = Table.Builder().addRow("Property", "Description")

        val classPropertyTraverser = ClassPropertiesDocumentationGenerator()
        val fields =
            classPropertyTraverser
                .getProperties(example, 5)
                .stream()
                .sorted(
                    Comparator.comparing(
                        ClassPropertiesDocumentationGenerator.PropertyInfo::dotNotationPath))
                .toList()
        val items: MutableList<Any?> = ArrayList()
        for (property in fields) {
            tableBuilder.addRow(
                property.dotNotationPath,
                if (property.description == null || property.description.isEmpty()) {
                    ""
                } else {
                    property.description
                },
            )
            items.add(UnorderedList(getProperties(property)))
        }

        return (tableBuilder.build().serialize() +
            "\n\n" +
            unindentYamlLines(UnorderedList<Any?>(items).toString(), "^  "))
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

        return String.format("%s:\n%s", exampleRoot, indentYamlLines(yamlContent, "  "))
    }

    companion object {
        private fun getProperties(
            property: ClassPropertiesDocumentationGenerator.PropertyInfo
        ): List<Any> {
            val detailList: MutableList<String?> = ArrayList()
            if (property.description != null && !property.description.isEmpty()) {
                detailList.add(property.description)
            }
            if (property.example != null && !property.example.isEmpty()) {
                detailList.add(
                    String.format(
                        "**Example**: ``` %s ```",
                        property.example.replace("```".toRegex(), "````")))
            }

            if (property.defaultValue != null && !property.defaultValue.isEmpty()) {
                detailList.add(
                    String.format(
                        "**Default Value**: ``` %s ```",
                        property.defaultValue.replace("```".toRegex(), "````")))
            }
            if (detailList.isEmpty()) {
                return listOf("**" + property.dotNotationPath + "**")
            }
            return listOf("**" + property.dotNotationPath + "**", UnorderedList<String>(detailList))
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

        private fun indentYamlLines(yamlContent: String, indentation: String?): String =
            Arrays.stream(
                    yamlContent.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map { str: String? -> indentation + str }
                .collect(Collectors.joining("\n"))

        private fun unindentYamlLines(yamlContent: String, indentation: String): String =
            Arrays.stream(
                    yamlContent.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .map { str: String? -> str!!.replaceFirst(indentation.toRegex(), "") }
                .collect(Collectors.joining("\n"))
    }
}
