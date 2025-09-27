package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.util.stream.Collectors
import junit.framework.TestCase
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.symfony.list.DoctrineEntityService

class DoctrineEntityListTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/testData"

    fun testItGetRoutesFromAttributes() {
        myFixture.copyDirectoryToProject("src", "/src")
        val doctrineEntityListService = loadPluginConfiguration(testDataPath)
        val lists = doctrineEntityListService.getElements()
        if (lists == null) {
            fail("list is empty")
        }
        val formattedList = lists!!.map { it?.name.toString() }.sorted().joinToString(",")
        val expectedList =
            ArrayList(mutableListOf<String?>("article__article", "article__feed"))
                .stream()
                .sorted()
                .collect(Collectors.joining(","))
        TestCase.assertEquals(expectedList, formattedList)
    }

    private fun loadPluginConfiguration(path: String?): DoctrineEntityService {
        try {
            val doctrineEntityListConfiguration =
                ConfigurationFactory()
                    .loadConfiguration(path, 0L, true)
                    ?.configuration
                    ?.doctrineEntitiesConfiguration
            val instance = DoctrineEntityService.getInstance(project)
            instance.loadConfiguration(doctrineEntityListConfiguration)

            return instance
        } catch (e: ConfigurationException) {
            throw RuntimeException(e)
        } catch (e: NoConfigurationFileException) {
            throw RuntimeException(e)
        }
    }
}
