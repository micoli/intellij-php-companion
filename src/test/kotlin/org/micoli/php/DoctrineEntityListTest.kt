package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.*
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.symfony.list.DoctrineEntityService

class DoctrineEntityListTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/symfony-demo"

    fun testItGetEntitiesFromAttributes() {
        myFixture.copyDirectoryToProject("src", "/src")
        val doctrineEntityListService = loadPluginConfiguration(testDataPath)
        val lists = doctrineEntityListService.getElements()
        val formattedList =
            lists.map { it.name }.filter { !it.contains("Proxies") }.sorted().joinToString(",")
        assertThat(formattedList)
            .isEqualTo("symfony_demo_comment,symfony_demo_post,symfony_demo_tag,symfony_demo_user")
    }

    private fun loadPluginConfiguration(path: String?): DoctrineEntityService {
        try {
            val doctrineEntityListConfiguration =
                ConfigurationFactory()
                    .loadConfiguration(path, 0L, true)
                    ?.configuration
                    ?.doctrineEntities
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
