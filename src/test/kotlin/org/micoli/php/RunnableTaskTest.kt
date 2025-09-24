package org.micoli.php

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException
import junit.framework.TestCase
import org.junit.Assert
import org.micoli.php.builders.BuiltinBuilder
import org.micoli.php.builders.ScriptBuilder
import org.micoli.php.builders.ShellBuilder
import org.micoli.php.tasks.runnables.RunnableTask

class RunnableTaskTest : BasePlatformTestCase() {
    fun testRunnableScriptSucceed() {
        // Given
        exposedVariable = 0

        // When
        RunnableTask(
                project,
                ScriptBuilder.create()
                    .withId("file1")
                    .withSource("org.micoli.php.RunnableTaskTest.exposedVariable++")
                    .build())
            .run()

        // Then
        TestCase.assertEquals(1, exposedVariable)
    }

    fun testRunnableShellSucceed() {
        // Given
        val runnableTask =
            RunnableTask(
                project, ShellBuilder.create().withId("file1").withCommand("ls / ").build())

        // Then
        val exception = Assert.assertThrows(NullPointerException::class.java) { runnableTask.run() }
        // At least we ensure that createShellWidget is well called
        TestCase.assertEquals("getOrInitToolWindow", exception.stackTrace[1].methodName)
    }

    @Throws(IOException::class, UnsupportedFlavorException::class)
    fun testRunnableBuiltinActionSucceed() {
        // Given
        val file = myFixture.addFileToProject("/test.txt", "ABCDEF")
        myFixture.openFileInEditor(file.virtualFile)
        myFixture.editor.selectionModel.setSelection(0, 3)

        // When
        RunnableTask(
                project, BuiltinBuilder.create().withId("file1").withActionId("\$Copy").build())
            .run()

        // Then
        val contents = CopyPasteManager.getInstance().contents
        assertNotNull(contents)
        assertEquals("ABC", contents!!.getTransferData(DataFlavor.stringFlavor))
    }

    companion object {
        var exposedVariable: Int = 0
    }
}
