package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.*
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.peerNavigation.service.PeerNavigationService
import org.micoli.php.service.intellij.psi.PhpUtil

class PeerNavigationServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/symfony-demo"

    fun testItFindsPeerElement() {
        myFixture.copyDirectoryToProject("src", "src")
        myFixture.copyDirectoryToProject("tests", "tests")
        val peerNavigationService = loadPluginConfiguration(testDataPath)
        val fqn1 = PhpUtil.getPhpClassByFQN(project, "\\App\\Controller\\BlogController")
        val fqn2 = PhpUtil.getPhpClassByFQN(project, "\\App\\Tests\\Controller\\BlogControllerTest")
        assertThat(fqn1).isNotNull
        assertThat(fqn2).isNotNull
        assertThat(peerNavigationService.getPeersElement(fqn1!!)?.first()).isEqualTo(fqn2)
        assertThat(peerNavigationService.getPeersElement(fqn2!!)?.first()).isEqualTo(fqn1)
    }

    fun testItCanFindLineMarkersFor() {
        myFixture.copyDirectoryToProject("src", "src")
        myFixture.copyDirectoryToProject("tests", "tests")
        myFixture.configureByFiles("src/Controller/BlogController.php")
        loadPluginConfiguration(testDataPath)
        val lineMarkers = myFixture.findAllGutters()
        assertThat(lineMarkers).isNotEmpty

        val specificMarkers =
            lineMarkers
                .stream()
                .filter {
                    val tooltipText = it!!.tooltipText ?: return@filter false
                    tooltipText.contains("Search for peer of [")
                }
                .toList()

        assertThat(specificMarkers.size).isEqualTo(1)
    }

    private fun loadPluginConfiguration(path: String?): PeerNavigationService {
        try {
            val peerNavigationConfiguration =
                ConfigurationFactory()
                    .loadConfiguration(path, 0L, true)
                    ?.configuration
                    ?.peerNavigation
            val instance = PeerNavigationService.getInstance(project)
            instance.loadConfiguration(peerNavigationConfiguration)

            return instance
        } catch (e: ConfigurationException) {
            throw RuntimeException(e)
        } catch (e: NoConfigurationFileException) {
            throw RuntimeException(e)
        }
    }
}
