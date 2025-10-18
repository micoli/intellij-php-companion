package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.symfony.list.RouteService

class RouteListTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/testData"

    fun testItGetRoutesFromAttributes() {
        myFixture.copyDirectoryToProject("src", "/src")
        val routeListService = loadPluginConfiguration(testDataPath)
        val formattedList = routeListService.getElements().map { it.uri }.sorted().joinToString(",")
        val expectedList =
            ArrayList(
                    mutableListOf<String?>(
                        "/api/article/{articleId}",
                        "/api/articles/feed/{feedId}",
                        "/api/articles/list",
                        "/api/articles/tag/{tag}",
                        "/api/articles/user",
                    ))
                .stream()
                .sorted()
                .toList()
                .joinToString(",")
        TestCase.assertEquals(expectedList, formattedList)
    }

    private fun loadPluginConfiguration(path: String?): RouteService {
        try {
            val routeListConfiguration =
                ConfigurationFactory()
                    .loadConfiguration(path, 0L, true)
                    ?.configuration
                    ?.routesConfiguration
            val instance = RouteService.getInstance(project)
            instance.loadConfiguration(routeListConfiguration)

            return instance
        } catch (e: ConfigurationException) {
            throw RuntimeException(e)
        } catch (e: NoConfigurationFileException) {
            throw RuntimeException(e)
        }
    }
}
