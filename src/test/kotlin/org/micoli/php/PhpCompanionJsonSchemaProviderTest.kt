package org.micoli.php

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.jsonSchema.JsonSchemaMappingsProjectConfiguration
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import com.jetbrains.jsonSchema.impl.inspections.JsonSchemaComplianceInspection
import java.io.IOException
import junit.framework.TestCase
import org.jetbrains.yaml.schema.YamlJsonSchemaHighlightingInspection
import org.micoli.php.configuration.schema.PhpCompanionJsonSchemaProviderFactory
import org.micoli.php.configuration.schema.PhpCompanionJsonSchemaProviderFactory.PhpCompanionJsonSchemaProvider
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

class PhpCompanionJsonSchemaProviderTest : BasePlatformTestCase() {
    private val invalidYamlContent =
        """
      peerNavigation:
        peers:
          - source: 123  # Error: should be a string
            target: App\Repository\*Repository
        associates:
          - classA: App\Entity\User
            invalidProperty: "This should not exist"  # invalid property
      symfonyMessenger:
        projectRootNamespace: true  # Error: should be a string
        messageInterfaces: "not an array"  # Error: should be an array

      """
            .trimIndent()

    override fun getTestDataPath(): String = "src/test/resources/symfony-demo"

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        InspectionProfileImpl.INIT_INSPECTIONS = true
        myFixture.enableInspections(
            *arrayOf(
                RequiredAttributesInspection(),
                JsonSchemaComplianceInspection(),
                YamlJsonSchemaHighlightingInspection(),
            ))
    }

    fun testInvalidYamlConfiguration() {
        val yamlFile =
            myFixture.configureByText(".php-companion.yaml", invalidYamlContent).virtualFile

        val schemaFiles = JsonSchemaService.Impl.get(project).getSchemaFilesForFile(yamlFile)
        assertFalse("Schema must be found here", schemaFiles.isEmpty())
        assertTrue(
            "File must be accepted by the provider",
            PhpCompanionJsonSchemaProvider().isAvailable(yamlFile))
    }

    fun testIfProviderIsWiredCorrectly() {
        val factory: JsonSchemaProviderFactory = PhpCompanionJsonSchemaProviderFactory()
        val providers = factory.getProviders(project)
        assertFalse("At least a provider must be provided", providers.isEmpty())

        val service = JsonSchemaService.Impl.get(project)
        assertNotNull("A JSON schema service should be available", service)

        val mappings = JsonSchemaMappingsProjectConfiguration.getInstance(project)
        assertNotNull("Mappings should be available", mappings)
    }

    @Throws(IOException::class)
    fun testItValidateWithExtraProperty() {
        myFixture.configureByText(
            ".php-companion.yaml",
            """
        aa: azeerty

        """
                .trimIndent(),
        )
        val highlights = myFixture.doHighlighting()
        assertNotEmpty(highlights)
    }

    fun testItValidateConfigurationInvalidFile() {
        myFixture.configureByText(".php-companion.yaml", invalidYamlContent)

        val highlights = myFixture.doHighlighting()
        TestCase.assertEquals(
            """
        2: - source: 123  # Error: should be a string [123] Schema validation: Incompatible types.
         Required: string. Actual: integer.
        8: projectRootNamespace: true  # Error: should be a string [true] Schema validation: Incompatible types.
         Required: string. Actual: boolean.
        9: messageInterfaces: "not an array"  # Error: should be an array ["not an array"] Schema validation: Incompatible types.
         Required: array. Actual: string.

        """
                .trimIndent()
                .trim { it <= ' ' },
            formatHighlights(myFixture.editor.document, highlights).trim { it <= ' ' },
        )
    }

    private fun formatHighlights(
        document: Document,
        highlights: MutableList<HighlightInfo?>
    ): String =
        highlights
            .stream()
            .map { i: HighlightInfo? ->
                val lineNumber = document.getLineNumber(i!!.getStartOffset())
                val lineStart = document.getLineStartOffset(lineNumber)
                val lineEnd = document.getLineEndOffset(lineNumber)
                val lineContent = document.getText(TextRange(lineStart, lineEnd)).trim { it <= ' ' }
                String.format("%s: %s [%s] %s", lineNumber, lineContent, i.text, i.description)
            }
            .toList()
            .joinToString("\n")

    @Throws(IOException::class)
    fun testItGeneratesProperSchema() {
        val dumperOptions = DumperOptions()
        dumperOptions.indent = 2
        dumperOptions.isPrettyFlow = true
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        Yaml(dumperOptions)
            .dump(ObjectMapper().readValue(generateJsonSchemaThoughProvider(), Any::class.java))
    }

    @Throws(IOException::class)
    private fun generateJsonSchemaThoughProvider(): String? =
        PhpCompanionJsonSchemaProviderFactory().getProviders(project).first()?.schemaFile?.let {
            VfsUtilCore.loadText(it)
        }
}
