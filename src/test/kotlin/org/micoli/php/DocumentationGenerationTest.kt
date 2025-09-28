package org.micoli.php

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.nio.file.Files
import java.nio.file.Path
import junit.framework.TestCase
import org.micoli.php.configuration.documentation.InstanceGenerator
import org.micoli.php.configuration.documentation.MarkdownProcessor
import org.micoli.php.configuration.documentation.MarkdownSchemaGenerator
import org.micoli.php.configuration.documentation.sourceCode.SourceDocumentationGenerator
import org.micoli.php.configuration.models.Configuration

class DocumentationGenerationTest : TestCase() {
    private val initial =
        """
      <!-- generateDocumentation%s("org.micoli.php.examples.TestConfiguration","%s") -->
      xxxxx
      <!-- generateDocumentationEnd -->
      """
            .trimIndent()
            .trim()

    fun testItGeneratesExampleWithoutRoot() {
        val processor = MarkdownProcessor()
        val expected =
            """
        <!-- generateDocumentationExample("org.micoli.php.examples.TestConfiguration","") -->
        ```yaml
        aBooleanValue: false
        aSubConfiguration:
        - aProperty1: default value of property1
          aProperty2: ''
        aSubSubConfiguration:
        - type: ClassA
          label: default value of property1
          classADescription: default value of property1
        - type: ClassB
          label: default value of property1
          classBDescription: default value of property1

        ```
        <!-- generateDocumentationEnd -->
        """
                .trimIndent()
                .trim()

        val result = processor.processContent(String.format(initial, "Example", ""))

        // Then it should change
        assertEquals(expected, result)

        // Then it should not change twice
        val result2 = processor.processContent(result)
        assertEquals(result, result2)
    }

    fun testItGeneratesExampleWithRoot() {
        val processor = MarkdownProcessor()
        val expected =
            """
        <!-- generateDocumentationExample("org.micoli.php.examples.TestConfiguration","exampleRoot") -->
        ```yaml
        exampleRoot:
          aBooleanValue: false
          aSubConfiguration:
          - aProperty1: default value of property1
            aProperty2: ''
          aSubSubConfiguration:
          - type: ClassA
            label: default value of property1
            classADescription: default value of property1
          - type: ClassB
            label: default value of property1
            classBDescription: default value of property1
        ```
        <!-- generateDocumentationEnd -->
        """
                .trimIndent()
                .trim()

        val result = processor.processContent(String.format(initial, "Example", "exampleRoot"))

        // Then it should change
        assertEquals(expected, result)

        // Then it should not change twice
        val result2 = processor.processContent(result)
        assertEquals(result, result2)
    }

    fun testItGeneratesProperties() {
        val processor = MarkdownProcessor()
        val expected =
            """
        <!-- generateDocumentationProperties("org.micoli.php.examples.TestConfiguration","") -->
        | Property                                 | Description              |
        | ---------------------------------------- | ------------------------ |
        | aBooleanValue                            | description of property1 |
        | aSubConfiguration[]                      |                          |
        | aSubConfiguration[].aProperty1           | description of property1 |
        | aSubConfiguration[].aProperty2           |                          |
        | aSubSubConfiguration[]                   |                          |
        | aSubSubConfiguration[].classADescription | description of property1 |
        | aSubSubConfiguration[].label             | description of property1 |
        
        - **aBooleanValue**
          - description of property1
          - **Example**: ``` example value of property1 ```
          - **Default Value**: ``` false ```
        - **aSubConfiguration[]**
        - **aSubConfiguration[].aProperty1**
          - description of property1
          - **Example**: ``` example value of property1 ```
          - **Default Value**: ``` default value of property1 ```
        - **aSubConfiguration[].aProperty2**
        - **aSubSubConfiguration[]**
        - **aSubSubConfiguration[].classADescription**
          - description of property1
          - **Example**: ``` example value of property1 ```
          - **Default Value**: ``` default value of property1 ```
        - **aSubSubConfiguration[].label**
          - description of property1
          - **Example**: ``` example value of property1 ```
          - **Default Value**: ``` default value of property1 ```
        <!-- generateDocumentationEnd -->
        """
                .trimIndent()
                .trim()

        val result = processor.processContent(String.format(initial, "Properties", ""))

        // Then it should change
        assertEquals(expected, result)

        // Then it should not change twice
        val result2 = processor.processContent(result)
        assertEquals(result, result2)
    }

    fun testItGeneratesDescription() {
        val processor = MarkdownProcessor()
        val expected =
            """
        <!-- generateDocumentationDescription("org.micoli.php.examples.TestConfiguration","") -->
        description of TestConfiguration
        <!-- generateDocumentationEnd -->
        """
                .trimIndent()
                .trim()

        val result = processor.processContent(String.format(initial, "Description", ""))

        // Then it should change
        assertEquals(expected, result)

        // Then it should not change twice
        val result2 = processor.processContent(result)
        assertEquals(result, result2)
    }

    @Throws(JsonProcessingException::class)
    fun testItGeneratesExampleAsJson() {
        val example: Any? = (InstanceGenerator()).get(Configuration::class.java, true)
        val mapper = ObjectMapper()
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        mapper.writeValueAsString(example)
    }

    fun testItGeneratesDescriptionProperties() {
        val markdownSchemaGenerator = MarkdownSchemaGenerator()
        markdownSchemaGenerator.generateMarkdownExample(Configuration::class.java, "root")
    }

    fun testItGeneratesDescriptionExample() {
        val markdownSchemaGenerator = MarkdownSchemaGenerator()
        markdownSchemaGenerator.generateMarkdownProperties(Configuration::class.java)
    }

    fun testItVerifyReadmeMdIsUpToDate() {
        val processor = MarkdownProcessor()
        val readmeMdPath = "./README.md"
        val existingReadMeContent = Files.readString(Path.of(readmeMdPath))
        val newContent = processor.processFile(readmeMdPath)
        assertEquals(trimLines(existingReadMeContent), trimLines(newContent))
    }

    fun testItGenerateJavadocDocumentation() {
        val newContent =
            SourceDocumentationGenerator.generateMarkdownDocumentation("org.micoli.php.scripting")
        assertTrue(newContent.contains("#### `Core`"))
    }

    fun testItGeneratesSourceDocumentation() {
        val processor = MarkdownProcessor()
        val expected =
            """
            <!-- generateDocumentationSource("org.micoli.php.examples.javaDocumentation","") -->
            #### `Example`
            
            - **Example****
            - **aMethod****
               - `aParameter`: String
            
            - **anotherMethod****
               - `anotherParameter`: String
               - `yetAnotherParameter`: Int
            
            
            <!-- generateDocumentationEnd -->
            """
                .trimIndent()
                .trim()

        val result =
            processor.processContent(
                """
            <!-- generateDocumentationSource("org.micoli.php.examples.javaDocumentation","") -->
            xxxxx
            <!-- generateDocumentationEnd -->
            """
                    .trimIndent()
                    .trim())

        // Then it should change
        assertEquals(expected, result)

        // Then it should not change twice
        val result2 = processor.processContent(result)
        assertEquals(result, result2)
    }

    companion object {
        private fun trimLines(existingReadMeContent: String): String =
            existingReadMeContent
                .split("\\n".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
                .map { obj: String? -> obj!!.trimEnd() }
                .toList()
                .joinToString("\n")
    }
}
