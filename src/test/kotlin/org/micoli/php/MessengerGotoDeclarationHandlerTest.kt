package org.micoli.php

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.*
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.symfony.messenger.navigation.MessengerGotoDeclarationHandler
import org.micoli.php.symfony.messenger.service.MessengerService

class MessengerGotoDeclarationHandlerTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/symfony-demo/"

    fun testItDetectDispatchMethods() {
        myFixture.copyDirectoryToProject("src", "/src")
        MessengerService.getInstance(project)
            .loadConfiguration(
                ConfigurationFactory()
                    .loadConfiguration(testDataPath, 0L, true)
                    ?.configuration
                    ?.symfonyMessenger)
        val file = myFixture.configureByFiles("src/Controller/BlogController.php")[0]
        assertGotoIsFound(file, "->query(", "src/UseCase/ListArticles/Handler.php")
        assertGotoIsFound(
            file,
            "->dispatch(new ArticleViewed\\Event(\$id))",
            "src/UseCase/ArticleViewed/Handler.php")
        assertGotoIsNull(file, "->handle(")
        assertGotoIsNull(file, "->queryBus")
    }

    private fun assertGotoIsNull(file: PsiFile, elementMatch: String) {
        val pos = file.text.indexOf(elementMatch)
        val element = file.findElementAt(pos + 2)
        val messengerGotoDeclarationHandler = MessengerGotoDeclarationHandler()
        assertThat(
                messengerGotoDeclarationHandler.getGotoDeclarationTargets(
                    element, 0, myFixture.editor))
            .isNull()
    }

    private fun assertGotoIsFound(
        file: PsiFile,
        elementMatch: String,
        targetFileEnd: String,
        targetMethodStart: String = "public function __invoke",
    ) {
        val pos = file.text.indexOf(elementMatch)
        val element = file.findElementAt(pos + 2)
        val messengerGotoDeclarationHandler = MessengerGotoDeclarationHandler()
        val foundElements: Array<PsiElement?> =
            messengerGotoDeclarationHandler.getGotoDeclarationTargets(element, 0, myFixture.editor)
                ?: emptyArray()
        assertThat(foundElements).isNotNull
        assertThat(foundElements.size).isEqualTo(1)
        assertThat(foundElements[0]!!.containingFile.virtualFile.canonicalPath!!)
            .endsWith(targetFileEnd)
        assertThat(foundElements[0]!!.text).startsWith(targetMethodStart)
    }
}
