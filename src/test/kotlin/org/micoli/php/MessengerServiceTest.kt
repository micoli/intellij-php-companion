package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.*
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.service.intellij.psi.PhpUtil
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration
import org.micoli.php.symfony.messenger.service.MessengerService

class MessengerServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/symfony-demo"

    fun testItDetectMessageBasedOnPatternClass() {
        myFixture.configureByFile("/src/UseCase/ArticleViewed/Event.php")
        val symfonyMessengerConfiguration = SymfonyMessengerConfiguration()
        symfonyMessengerConfiguration.messageClassNamePatterns = ".*(Event|Command)$"
        val messengerService = MessengerService.getInstance(project)
        messengerService.loadConfiguration(symfonyMessengerConfiguration)
        val phpClass = PhpUtil.getPhpClassByFQN(project, "App\\UseCase\\ArticleViewed\\Event")
        assertThat(phpClass).isNotNull
        assertThat(messengerService.isMessageClass(phpClass!!)).isTrue
    }

    fun testItDoesNotDetectMessageBasedOnPatternIfPatternIsWrong() {
        myFixture.configureByFile("/src/UseCase/ArticleViewed/Event.php")
        val symfonyMessengerConfiguration = SymfonyMessengerConfiguration()
        symfonyMessengerConfiguration.messageClassNamePatterns = ".*(Command)$"
        val messengerService = MessengerService.getInstance(project)
        messengerService.loadConfiguration(symfonyMessengerConfiguration)
        val phpClass = PhpUtil.getPhpClassByFQN(project, "App\\UseCase\\ArticleViewed\\Event")
        assertNotNull(phpClass)
        assertFalse(messengerService.isMessageClass(phpClass!!))
    }

    fun testItDetectMessageBasedOnInterface() {
        myFixture.configureByFiles(
            "/src/UseCase/ArticleViewed/Event.php",
            "/src/Infrastructure/Bus/Message/Event/DomainEvent.php",
            "/src/Infrastructure/Bus/Message/Command/DomainCommand.php",
            "/src/Infrastructure/Bus/Message/Command/SyncDomainCommandResult.php",
            "/src/Infrastructure/Bus/Message/Query/SyncDomainQueryResult.php",
            "/src/Infrastructure/Bus/Message/Command/SyncDomainCommand.php",
            "/src/Infrastructure/Bus/Message/Event/SyncDomainEvent.php",
            "/src/Infrastructure/Bus/Message/DomainMessage.php",
            "/src/Infrastructure/Bus/Message/Query/SyncDomainQuery.php",
            "/src/Infrastructure/Bus/Message/Query/DomainQuery.php",
            "/src/Infrastructure/Bus/Message/Command/SyncDomainCommandWithResult.php",
            "/src/Infrastructure/Bus/Message/Event/AsyncDomainEvent.php",
            "/src/Infrastructure/Bus/Message/Command/AsyncDomainCommand.php",
        )
        val symfonyMessengerConfiguration = SymfonyMessengerConfiguration()
        symfonyMessengerConfiguration.messageInterfaces =
            arrayOf("App\\Infrastructure\\Bus\\Message\\DomainMessage")
        val messengerService = MessengerService.getInstance(project)
        messengerService.loadConfiguration(symfonyMessengerConfiguration)
        val phpClass = PhpUtil.getPhpClassByFQN(project, "App\\UseCase\\ArticleViewed\\Event")
        assertThat(phpClass).isNotNull
        assertThat(messengerService.isMessageClass(phpClass!!)).isTrue
    }

    fun testItCanFindHandlersByMessage() {
        myFixture.copyDirectoryToProject("src", "src")
        val messengerService = loadPluginConfiguration(testDataPath)
        val handledMessages =
            messengerService.findHandlersByMessageName("App\\UseCase\\ArticleViewed\\Event")
        assertThat(handledMessages.stream().map { it!!.fqn }.toList())
            .contains("\\App\\UseCase\\ArticleViewed\\Handler.__invoke")
    }

    fun testItCanFindLineMarkersForMessageHandler() {
        myFixture.copyDirectoryToProject("src", "src")
        myFixture.configureByFiles("src/UseCase/ArticleViewed/Handler.php")
        loadPluginConfiguration(testDataPath)
        val lineMarkers = myFixture.findAllGutters()
        assertThat(lineMarkers).isNotEmpty

        val specificMarkers =
            lineMarkers
                .stream()
                .filter {
                    val tooltipText = it!!.tooltipText ?: return@filter false
                    tooltipText.contains("Search for usages") ||
                        tooltipText.contains("Navigate to message handlers")
                }
                .toList()
        assertThat(specificMarkers.size).isEqualTo(1)
    }

    fun testItCanFindDispatchCallsForMessageClass() {
        myFixture.copyDirectoryToProject("src", "src")
        val messengerService = loadPluginConfiguration(testDataPath)
        val callsWithoutRootNamespace =
            messengerService.findDispatchCallsForMessage("App\\UseCase\\ArticleViewed\\Event")
        val callsWithRootNamespace =
            messengerService.findDispatchCallsForMessage("\\App\\UseCase\\ArticleViewed\\Event")

        assertThat(callsWithoutRootNamespace.size).isEqualTo(2)
        assertThat(callsWithoutRootNamespace.size).isEqualTo(callsWithRootNamespace.size)
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
