package org.micoli.php

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.symfony.messenger.navigation.MessengerGotoDeclarationHandler
import org.micoli.php.symfony.messenger.service.MessengerService

class MessengerGotoDeclarationHandlerTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/resources/testData/"
    }

    @Throws(NoConfigurationFileException::class, ConfigurationException::class)
    fun testItDetectDispatchMethods() {
        myFixture.copyDirectoryToProject("src", "/src")
        MessengerService.getInstance(project).loadConfiguration(ConfigurationFactory.loadConfiguration(testDataPath, 0L, true).configuration.symfonyMessenger)
        val files = myFixture.configureByFiles("src/UserInterface/Web/Api/Article/List/Controller.php")
        assertGotoEquals(files[0]!!, "->query(", "src/Core/Query/Article/Handler.php")
        assertGotoEquals(files[0]!!, "->notify(", "src/Core/Query/Article/Handler.php")
        assertGotoEquals(files[0]!!, "->query(new ArticleDetails\\Query())", "src/Core/Query/ArticleDetails/Handler.php")
        assertGotoIsNull(files[0]!!, "->handle(")
        assertGotoIsNull(files[0]!!, "->dispatch")
        assertGotoIsNull(files[0]!!, "->queryBus")
    }

    private fun assertGotoIsNull(file: PsiFile, elementMatch: String) {
        val pos = file.text.indexOf(elementMatch)
        val element = file.findElementAt(pos + 2)
        val messengerGotoDeclarationHandler = MessengerGotoDeclarationHandler()
        assertNull(messengerGotoDeclarationHandler.getGotoDeclarationTargets(element, 0, myFixture.editor))
    }

    private fun assertGotoEquals(file: PsiFile, elementMatch: String, targetFileEnd: String, targetMethodStart: String = "public function __invoke") {
        val pos = file.text.indexOf(elementMatch)
        val element = file.findElementAt(pos + 2)
        val messengerGotoDeclarationHandler = MessengerGotoDeclarationHandler()
        val foundElements = messengerGotoDeclarationHandler.getGotoDeclarationTargets(element, 0, myFixture.editor)
        assertNotNull(foundElements)
        TestCase.assertEquals(1, foundElements!!.size)
        assertTrue(foundElements[0]!!.containingFile.virtualFile.canonicalPath!!.endsWith(targetFileEnd))
        assertTrue(foundElements[0]!!.text.startsWith(targetMethodStart))
    }
}
