package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.*
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.symfony.list.RouteService

class RouteListTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/symfony-demo"

    fun testItGetRoutesFromAttributes() {
        myFixture.copyDirectoryToProject("src", "/src")
        val routeListService = loadPluginConfiguration(testDataPath)
        val routes =
            routeListService.getElements().map { "${it.methods}-${it.uri}-${it.name}" }.sorted()
        assertThat(routes)
            .contains("GET, POST-/change-password-user_change_password")
            .contains("-/login-security_login")
            .contains("GET-/page/{page}-blog_index_paginated")
            .size()
            .isEqualTo(18)
    }

    private fun loadPluginConfiguration(path: String?): RouteService {
        try {
            val routeListConfiguration =
                ConfigurationFactory().loadConfiguration(path, 0L, true)?.configuration?.routes
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
