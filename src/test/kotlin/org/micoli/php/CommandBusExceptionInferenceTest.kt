package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.*
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.symfony.messenger.annotator.CommandBusExceptionService
import org.micoli.php.symfony.messenger.service.MessengerService

class CommandBusExceptionInferenceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/symfony-demo"

    fun testExtractThrowsFromHandlerWithThrows() {
        myFixture.copyDirectoryToProject("src", "src")
        loadPluginConfiguration(testDataPath)

        val phpClass =
            org.micoli.php.service.intellij.psi.PhpUtil.getPhpClassByFQN(
                project, "App\\UseCase\\ArticleViewed\\HandlerWithThrows")
        assertThat(phpClass).isNotNull

        val invokeMethod = phpClass!!.findOwnMethodByName("__invoke")
        assertThat(invokeMethod).isNotNull

        val throwsTags = CommandBusExceptionService.extractThrowsFromDocComment(invokeMethod!!)
        assertThat(throwsTags)
            .containsExactlyInAnyOrder(
                "\\App\\Exception\\ArticleNotFoundException",
                "\\App\\Exception\\PermissionDeniedException")
    }

    fun testExtractThrowsFromHandlerWithoutThrows() {
        myFixture.copyDirectoryToProject("src", "src")
        loadPluginConfiguration(testDataPath)

        val phpClass =
            org.micoli.php.service.intellij.psi.PhpUtil.getPhpClassByFQN(
                project, "App\\UseCase\\ArticleViewed\\Handler")
        assertThat(phpClass).isNotNull

        val invokeMethod = phpClass!!.findOwnMethodByName("__invoke")
        assertThat(invokeMethod).isNotNull

        val throwsTags = CommandBusExceptionService.extractThrowsFromDocComment(invokeMethod!!)
        assertThat(throwsTags).isEmpty()
    }

    fun testExtractThrowsFromControllerWithPartialThrows() {
        myFixture.copyDirectoryToProject("src", "src")
        loadPluginConfiguration(testDataPath)

        val phpClass =
            org.micoli.php.service.intellij.psi.PhpUtil.getPhpClassByFQN(
                project, "App\\Controller\\ControllerWithPartialThrows")
        assertThat(phpClass).isNotNull

        val showMethod = phpClass!!.findOwnMethodByName("showAction")
        assertThat(showMethod).isNotNull

        val throwsTags = CommandBusExceptionService.extractThrowsFromDocComment(showMethod!!)
        assertThat(throwsTags).containsExactly("\\App\\Exception\\ArticleNotFoundException")
    }

    fun testExtractThrowsFromControllerWithAllThrows() {
        myFixture.copyDirectoryToProject("src", "src")
        loadPluginConfiguration(testDataPath)

        val phpClass =
            org.micoli.php.service.intellij.psi.PhpUtil.getPhpClassByFQN(
                project, "App\\Controller\\ControllerWithAllThrows")
        assertThat(phpClass).isNotNull

        val showMethod = phpClass!!.findOwnMethodByName("showAction")
        assertThat(showMethod).isNotNull

        val throwsTags = CommandBusExceptionService.extractThrowsFromDocComment(showMethod!!)
        assertThat(throwsTags)
            .containsExactlyInAnyOrder(
                "\\App\\Exception\\ArticleNotFoundException",
                "\\App\\Exception\\PermissionDeniedException")
    }

    fun testExtractCaughtExceptionsFromTryCatch() {
        myFixture.copyDirectoryToProject("src", "src")
        loadPluginConfiguration(testDataPath)

        val psiFile =
            myFixture.configureByFiles("src/Controller/ControllerWithTryCatch.php").first()

        val dispatchCalls =
            com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(
                psiFile, com.jetbrains.php.lang.psi.elements.MethodReference::class.java)
        val dispatchCall = dispatchCalls.first { it.name == "dispatch" }

        val caughtExceptions = CommandBusExceptionService.extractCaughtExceptions(dispatchCall)
        assertThat(caughtExceptions).containsExactly("\\App\\Exception\\ArticleNotFoundException")
    }

    fun testExtractCaughtExceptionsFromTryCatchAll() {
        myFixture.copyDirectoryToProject("src", "src")
        loadPluginConfiguration(testDataPath)

        val psiFile =
            myFixture.configureByFiles("src/Controller/ControllerWithTryCatchAll.php").first()

        val dispatchCalls =
            com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(
                psiFile, com.jetbrains.php.lang.psi.elements.MethodReference::class.java)
        val dispatchCall = dispatchCalls.first { it.name == "dispatch" }

        val caughtExceptions = CommandBusExceptionService.extractCaughtExceptions(dispatchCall)
        assertThat(caughtExceptions)
            .containsExactlyInAnyOrder(
                "\\App\\Exception\\ArticleNotFoundException",
                "\\App\\Exception\\PermissionDeniedException")
    }

    fun testNoCaughtExceptionsWithoutTryCatch() {
        myFixture.copyDirectoryToProject("src", "src")
        loadPluginConfiguration(testDataPath)

        val psiFile =
            myFixture.configureByFiles("src/Controller/ControllerWithMissingThrows.php").first()

        val dispatchCalls =
            com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(
                psiFile, com.jetbrains.php.lang.psi.elements.MethodReference::class.java)
        val dispatchCall = dispatchCalls.first { it.name == "dispatch" }

        val caughtExceptions = CommandBusExceptionService.extractCaughtExceptions(dispatchCall)
        assertThat(caughtExceptions).isEmpty()
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

            CommandBusExceptionService.getInstance(project).enabled =
                symfonyMessengerConfiguration?.enableExceptionInference ?: false

            return messengerService
        } catch (e: ConfigurationException) {
            throw RuntimeException(e)
        } catch (e: NoConfigurationFileException) {
            throw RuntimeException(e)
        }
    }
}
