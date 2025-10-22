package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.*
import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration
import org.micoli.php.attributeNavigation.configuration.NavigationByAttributeRule
import org.micoli.php.attributeNavigation.service.AttributeNavigationService
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException

class AttributeNavigationTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/symfony-demo"

    fun testItFormatValueUsingInlineFormatter() {
        val formattedValue =
            AttributeNavigationService.getInstance(project)
                .getFormattedValue("cde", "return ('ab-'+value+'-fg').toLowerCase()")
        assertThat(formattedValue).isEqualTo("ab-cde-fg")
    }

    fun testItCanFindLineMarkersForAttributes() {
        myFixture.configureByFiles("src/Controller/BlogController.php")
        loadPluginConfiguration(testDataPath)
        val lineMarkers = myFixture.findAllGutters()
        assertThat(lineMarkers).isNotEmpty

        val specificMarkers =
            lineMarkers
                .stream()
                .map { it?.tooltipText?.replace("Search for ", "") }
                .toList()
                .filterNotNull()
                .sorted()
                .joinToString(",")

        assertThat(specificMarkers)
            .isEqualTo(
                "[/],[/blog],[/comment/{postSlug}/new],[/page/{page}],[/posts/{slug:post}],[/rss.xml],[/search]")
    }

    fun testItFormatValueUsingScriptInConfiguration() {
        val instance = loadPluginConfiguration(testDataPath)
        val rule: NavigationByAttributeRule = instance.rules.first()
        val formattedValue =
            instance.getFormattedValue(
                "/templates/{templateId}/documents/{documentId}", rule.formatterScript)
        assertEquals("/templates/[^/]*/documents/[^/]*:", formattedValue)
    }

    private fun loadPluginConfiguration(path: String): AttributeNavigationService {
        var attributeNavigationConfiguration: AttributeNavigationConfiguration?
        try {
            attributeNavigationConfiguration =
                ConfigurationFactory()
                    .loadConfiguration(path, 0L, true)
                    ?.configuration
                    ?.attributeNavigation
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
