package org.micoli.php

import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.peerNavigation.service.PeerNavigationService
import org.micoli.php.service.intellij.psi.PhpUtil

class PeerNavigationServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/testData"

    fun testItFindsPeerElement() {
        myFixture.copyDirectoryToProject("src", "src")
        myFixture.copyDirectoryToProject("tests", "tests")
        val peerNavigationService = loadPluginConfiguration(testDataPath)
        val fqn1 =
            PhpUtil.getPhpClassByFQN(
                project, "\\App\\UserInterface\\Web\\Api\\Article\\Get\\Controller")
        val fqn2 =
            PhpUtil.getPhpClassByFQN(
                project,
                "\\App\\Tests\\Func\\UserInterface\\Web\\Api\\Article\\Get\\ControllerTest")
        assertNotNull(fqn1)
        assertNotNull(fqn2)
        assertEquals(fqn2, peerNavigationService.getPeersElement(fqn1!!)?.first())
        assertEquals(fqn1, peerNavigationService.getPeersElement(fqn2!!)?.first())
    }

    fun testItCanFindLineMarkersFor() {
        myFixture.copyDirectoryToProject("src", "src")
        myFixture.copyDirectoryToProject("tests", "tests")
        myFixture.configureByFiles("src/UserInterface/Web/Api/Article/Get/Controller.php")
        loadPluginConfiguration(testDataPath)
        val lineMarkers = myFixture.findAllGutters()
        assertNotEmpty(lineMarkers)

        val specificMarkers =
            lineMarkers
                .stream()
                .filter { it: GutterMark? ->
                    val tooltipText = it!!.tooltipText
                    if (tooltipText == null) {
                        return@filter false
                    }
                    tooltipText.contains("Search for peer of [")
                }
                .toList()

        TestCase.assertEquals(1, specificMarkers.size)
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
