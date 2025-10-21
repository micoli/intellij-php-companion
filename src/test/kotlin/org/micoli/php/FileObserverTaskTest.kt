package org.micoli.php

import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.IOException
import org.assertj.core.api.Assertions.*
import org.micoli.php.builders.ObservedFileBuilder
import org.micoli.php.builders.PostToggleScriptBuilder
import org.micoli.php.tasks.runnables.FileObserverTask
import org.micoli.php.ui.components.tasks.helpers.FileObserver

class FileObserverTaskTest : BasePlatformTestCase() {
    lateinit var filePath: String

    override fun setUp() {
        super.setUp()
        val fileContent =
            """
        variable1=value
        #variable2=value

        """
                .trimIndent()
        filePath = "config.env"
        myFixture.addFileToProject(filePath, fileContent)
    }

    fun testStatusToggleOnKnownActiveVariable() {
        // Given
        val fileObserverTask =
            FileObserverTask(
                project,
                ObservedFileBuilder.create()
                    .withId("file1")
                    .withFilePath(filePath)
                    .withVariableName("variable1")
                    .build(),
            )
        assertEquals(FileObserver.Status.Active, fileObserverTask.status)

        // When
        fileObserverTask.run()

        // Then
        assertEquals(FileObserver.Status.Inactive, fileObserverTask.status)
        assertThat(fileObserverTask.iconAndPrefix.getPrefix()).isEqualTo("# ")
        assertFileContentEquals(filePath, "#variable1=value\n#variable2=value")
    }

    fun testStatusToggleWithSpecificPrefix() {
        // Given
        val fileContent =
            """
        variable1=value

        """
                .trimIndent()
        val specificFilePath = "config2.env"
        myFixture.addFileToProject(specificFilePath, fileContent)
        val fileObserverTask =
            FileObserverTask(
                project,
                ObservedFileBuilder.create()
                    .withId("file1")
                    .withFilePath(specificFilePath)
                    .withCommentPrefix(";")
                    .withVariableName("variable1")
                    .build(),
            )
        assertEquals(FileObserver.Status.Active, fileObserverTask.status)

        // When
        fileObserverTask.run()

        // Then
        assertEquals(FileObserver.Status.Inactive, fileObserverTask.status)
        assertThat(fileObserverTask.iconAndPrefix.getPrefix()).isEqualTo("# ")
        assertFileContentEquals(specificFilePath, ";variable1=value")
    }

    fun testItLaunchPostAction() {
        // Given
        val fileObserverTask =
            FileObserverTask(
                project,
                ObservedFileBuilder.create()
                    .withId("file1")
                    .withFilePath(filePath)
                    .withVariableName("variable1")
                    .withPostToggle(
                        PostToggleScriptBuilder.create()
                            .withSource("org.micoli.php.FileObserverTaskTest.exposedVariable++")
                            .build())
                    .build(),
            )
        assertEquals(FileObserver.Status.Active, fileObserverTask.status)
        exposedVariable = 0

        // When
        fileObserverTask.run()

        // Then
        assertEquals(FileObserver.Status.Inactive, fileObserverTask.status)
        assertThat(exposedVariable).isEqualTo(1)

        // And when
        fileObserverTask.run()
        assertThat(exposedVariable).isEqualTo(2)
    }

    fun testItDoesNotLaunchPostActionIfActionFailed() {
        // Given
        val fileObserverTask =
            FileObserverTask(
                project,
                ObservedFileBuilder.create()
                    .withId("file1")
                    .withFilePath(filePath)
                    .withVariableName("unknownVariable")
                    .withPostToggle(
                        PostToggleScriptBuilder.create()
                            .withSource("org.micoli.php.FileObserverTaskTest.exposedVariable++")
                            .build())
                    .build(),
            )
        assertEquals(FileObserver.Status.Unknown, fileObserverTask.status)
        exposedVariable = 0

        // When
        fileObserverTask.run()

        // Then
        assertThat(exposedVariable).isEqualTo(0)
    }

    fun testStatusToggleOnKnownInactiveVariable() {
        // Given
        val fileObserverTask =
            FileObserverTask(
                project,
                ObservedFileBuilder.create()
                    .withId("file1")
                    .withFilePath(filePath)
                    .withVariableName("variable2")
                    .build(),
            )
        assertEquals(FileObserver.Status.Inactive, fileObserverTask.status)

        // When
        fileObserverTask.run()

        // Then
        assertEquals(FileObserver.Status.Active, fileObserverTask.status)
        assertThat(fileObserverTask.iconAndPrefix.getPrefix()).isEqualTo("")
        assertFileContentEquals(filePath, "variable1=value\nvariable2=value")
    }

    fun testStatusToggleOnUnknownVariable() {
        // Given
        val fileObserverTask =
            FileObserverTask(
                project,
                ObservedFileBuilder.create()
                    .withId("file1")
                    .withFilePath(filePath)
                    .withVariableName("unknownVariable")
                    .build(),
            )
        assertEquals(FileObserver.Status.Unknown, fileObserverTask.status)

        // When
        fileObserverTask.run()

        // Then
        assertEquals(FileObserver.Status.Unknown, fileObserverTask.status)
        assertThat(fileObserverTask.iconAndPrefix.getPrefix()).isEqualTo("? ")
    }

    fun testStatusToggleOnUnknownFile() {
        // Given
        val fileObserverTask =
            FileObserverTask(
                project,
                ObservedFileBuilder.create()
                    .withId("file1")
                    .withFilePath("unknown-config.env")
                    .withVariableName("variable")
                    .build(),
            )
        assertEquals(FileObserver.Status.Unknown, fileObserverTask.status)

        // When
        fileObserverTask.run()

        // Then
        assertEquals(FileObserver.Status.Unknown, fileObserverTask.status)
        assertThat(fileObserverTask.iconAndPrefix.getPrefix()).isEqualTo("? ")
    }

    private fun assertFileContentEquals(filepathPrm: String, content: String?) {
        try {
            assertThat(
                    VfsUtilCore.loadText(myFixture.findFileInTempDir(filepathPrm)).trim {
                        it <= ' '
                    },
                )
                .isEqualTo(content)
        } catch (_: IOException) {
            fail("File not found: $filepathPrm")
        }
    }

    companion object {
        var exposedVariable: Int = 0
    }
}
