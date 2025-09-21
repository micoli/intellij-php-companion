package org.micoli.php

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.replaceService
import java.awt.datatransfer.DataFlavor
import java.util.concurrent.atomic.AtomicReference
import junit.framework.TestCase
import org.micoli.php.builders.ScriptBuilder
import org.micoli.php.tasks.runnables.RunnableTask
import org.micoli.php.ui.Notification

class ScriptingCoreTest : BasePlatformTestCase() {
    fun testItRunCoreRunActionInEditor() {
        // Given
        exposedVariable = 0
        val file = myFixture.addFileToProject("/test.txt", "ABCDEF")
        myFixture.openFileInEditor(file.virtualFile)
        myFixture.editor.selectionModel.setSelection(0, 3)

        // When
        RunnableTask(
            project,
            ScriptBuilder.create()
              .withId("file1")
              .withSource(
                """
                                core.runActionInEditor("\${'$'}Copy");
                                org.micoli.php.ScriptingCoreTest.exposedVariable++

                    """
                  .trimIndent()
              )
              .build(),
          )
          .run()

        // Then
        val contents = CopyPasteManager.getInstance().contents
        assertNotNull(contents)
        assertEquals("ABC", contents!!.getTransferData(DataFlavor.stringFlavor))
        TestCase.assertEquals(1, exposedVariable)
    }

    fun testItReportsError() {
        val lastError: AtomicReference<String?> = AtomicReference<String?>()
        val lastMessage: AtomicReference<String?> = AtomicReference<String?>()
        val mockAppService: Notification =
          object : Notification(project) {
              override fun error(message: String?) {
                  lastError.set(message)
              }

              override fun message(message: String?) {
                  lastMessage.set(message)
              }
          }
        project.replaceService(Notification::class.java, mockAppService, testRootDisposable)
        lastError.set(null)
        lastMessage.set(null)

        RunnableTask(project, ScriptBuilder.create().withSource("org.micoli.php.ScriptingCoreTest.unUnknownExposedVariable++").build()).run()

        // Then
        TestCase.assertEquals("groovy.lang.MissingPropertyException: No such property: unUnknownExposedVariable for class:" + " org.micoli.php.ScriptingCoreTest", lastError.get())
    }

    companion object {
        var exposedVariable: Int = 0
    }
}
