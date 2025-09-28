package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.service.intellij.psi.PhpUtil
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration
import org.micoli.php.symfony.messenger.service.MessengerService

class MessengerServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/testData"

    fun testItDetectMessageBasedOnPatternClass() {
        myFixture.configureByFile("/src/Core/Event/ArticleCreatedEvent.php")
        val symfonyMessengerConfiguration = SymfonyMessengerConfiguration()
        symfonyMessengerConfiguration.messageClassNamePatterns = ".*(edEvent|Command)$"
        val messengerService = MessengerService.getInstance(project)
        messengerService.loadConfiguration(symfonyMessengerConfiguration)
        val phpClass = PhpUtil.getPhpClassByFQN(project, "App\\Core\\Event\\ArticleCreatedEvent")
        assertNotNull(phpClass)
        assertTrue(messengerService.isMessageClass(phpClass!!))
    }

    fun testItDoesNotDetectMessageBasedOnPatternIfPatternIsWrong() {
        myFixture.configureByFile("/src/Core/Event/ArticleCreatedEvent.php")
        val symfonyMessengerConfiguration = SymfonyMessengerConfiguration()
        symfonyMessengerConfiguration.messageClassNamePatterns = ".*(Command)$"
        val messengerService = MessengerService.getInstance(project)
        messengerService.loadConfiguration(symfonyMessengerConfiguration)
        val phpClass = PhpUtil.getPhpClassByFQN(project, "App\\Core\\Event\\ArticleCreatedEvent")
        assertNotNull(phpClass)
        assertFalse(messengerService.isMessageClass(phpClass!!))
    }

    fun testItDetectMessageBasedOnInterface() {
        myFixture.configureByFiles(
            "/src/Core/Event/ArticleCreatedEvent.php",
            "/src/Infrastructure/Bus/Message/Event/AsyncEventInterface.php",
            "/src/Infrastructure/Bus/Message/Event/EventInterface.php",
            "/src/Infrastructure/Bus/Message/MessageInterface.php",
        )
        val symfonyMessengerConfiguration = SymfonyMessengerConfiguration()
        symfonyMessengerConfiguration.messageInterfaces =
            arrayOf("App\\Infrastructure\\Bus\\Message\\MessageInterface")
        val messengerService = MessengerService.getInstance(project)
        messengerService.loadConfiguration(symfonyMessengerConfiguration)
        val phpClass = PhpUtil.getPhpClassByFQN(project, "App\\Core\\Event\\ArticleCreatedEvent")
        assertNotNull(phpClass)
        assertTrue(messengerService.isMessageClass(phpClass!!))
    }

    fun testItCanFindHandlersByMessage() {
        myFixture.copyDirectoryToProject("src", "src")
        val messengerService = loadPluginConfiguration(testDataPath)
        val handledMessages =
            messengerService.findHandlersByMessageName("App\\Core\\Event\\ArticleCreatedEvent")
        assertContainsElements(
            handledMessages.stream().map { it!!.fqn }.toList(),
            "\\App\\Core\\EventListener\\OnArticleCreated.__invoke",
        )
    }

    fun testItCanFindLineMarkersForMessageHandler() {
        myFixture.copyDirectoryToProject("src", "src")
        myFixture.configureByFiles("src/Core/EventListener/OnFeedCreated.php")
        loadPluginConfiguration(testDataPath)
        val lineMarkers = myFixture.findAllGutters()
        assertNotEmpty(lineMarkers)

        val specificMarkers =
            lineMarkers
                .stream()
                .filter {
                    val tooltipText = it!!.tooltipText ?: return@filter false
                    tooltipText.contains("Search for usages") ||
                        tooltipText.contains("Navigate to message handlers")
                }
                .toList()
        TestCase.assertEquals(2, specificMarkers.size)
    }

    fun testItCanFindDispatchCallsForMessageClass() {
        myFixture.copyDirectoryToProject("src", "src")
        val messengerService = loadPluginConfiguration(testDataPath)
        val callsWithoutRootNamespace =
            messengerService.findDispatchCallsForMessage("App\\Core\\Event\\ArticleCreatedEvent")
        val callsWithRootNamespace =
            messengerService.findDispatchCallsForMessage("\\App\\Core\\Event\\ArticleCreatedEvent")

        TestCase.assertEquals(1, callsWithoutRootNamespace.size)
        TestCase.assertEquals(callsWithRootNamespace.size, callsWithoutRootNamespace.size)
    }

    private fun loadPluginConfiguration(path: String?): MessengerService {
        try {
            val symfonyMessengerConfiguration =
                ConfigurationFactory()
                    .loadConfiguration(path, 0L, true)
                    ?.configuration
                    ?.symfonyMessenger
            val messengerService = MessengerService.getInstance(project)
            messengerService.loadConfiguration(symfonyMessengerConfiguration)

            return messengerService
        } catch (e: ConfigurationException) {
            throw RuntimeException(e)
        } catch (e: NoConfigurationFileException) {
            throw RuntimeException(e)
        }
    }
}
