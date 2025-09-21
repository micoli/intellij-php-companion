package org.micoli.php

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration
import org.micoli.php.attributeNavigation.configuration.NavigationByAttributeRule
import org.micoli.php.attributeNavigation.service.AttributeNavigationService
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException

class AttributeNavigationTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/testData"

    fun testItFormatValueUsingInlineFormatter() {
        val formattedValue = AttributeNavigationService.getInstance(project).getFormattedValue("cde", "return ('ab-'+value+'-fg').toLowerCase()")
        TestCase.assertEquals("ab-cde-fg", formattedValue)
    }

    fun testItCanFindLineMarkersForAttributes() {
        myFixture.configureByFiles("src/UserInterface/Web/Api/Article/Get/Controller.php")
        loadPluginConfiguration(testDataPath)
        val lineMarkers = myFixture.findAllGutters()
        assertNotEmpty(lineMarkers)

        val specificMarkers =
          lineMarkers
            .stream()
            .filter { it: GutterMark? ->
                val tooltipText = it?.tooltipText ?: return@filter false
                tooltipText.contains("Search for [")
            }
            .toList()

        TestCase.assertEquals(1, specificMarkers.size)
    }

    fun testItFormatValueUsingScriptInConfiguration() {
        val instance = loadPluginConfiguration(testDataPath)
        val rule: NavigationByAttributeRule = instance.rules.first()
        val formattedValue = instance.getFormattedValue("/templates/{templateId}/documents/{documentId}", rule.formatterScript)
        TestCase.assertEquals("/templates/[^/]*/documents/[^/]*:", formattedValue)
    }

    private fun loadPluginConfiguration(path: String?): AttributeNavigationService {
        var attributeNavigationConfiguration: AttributeNavigationConfiguration?
        try {
            attributeNavigationConfiguration = ConfigurationFactory().loadConfiguration(path, 0L, true)?.configuration?.attributeNavigation
        } catch (e: ConfigurationException) {
            throw RuntimeException(e)
        } catch (e: NoConfigurationFileException) {
            throw RuntimeException(e)
        }
        val instance = AttributeNavigationService.getInstance(project)
        instance.loadConfiguration(attributeNavigationConfiguration)

        return instance
    }
}
