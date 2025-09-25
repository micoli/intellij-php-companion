package org.micoli.php

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.micoli.php.service.filesystem.FileListProcessor

class FileListProcessorTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/testMarkDownExporterData"

    private fun initFixtures(withExclusionRules: Boolean) {
        if (withExclusionRules) {
            myFixture.addFileToProject(
                "/.aiignore",
                """
          # Ignore test file
          target/**
          out/**
          **/*Test.java

          """
                    .trimIndent(),
            )
        }

        myFixture.addFileToProject("/target/classes/App.class", "compiled content")
        myFixture.addFileToProject("/out/production/Main.class", "compiled content")
        myFixture.addFileToProject("/main/App.java", "source content")
        myFixture.addFileToProject("/main/AppTest.java", "source content")
    }

    fun testBasicFileListProcessor() {
        myFixture.copyDirectoryToProject(".", ".")
        val filesToSelect = listOf<VirtualFile>(myFixture.findFileInTempDir("/")).toMutableList()

        val processedFiles = FileListProcessor.findFilesFromSelectedFiles(filesToSelect)

        TestCase.assertEquals(4, processedFiles.size)
    }

    fun testFileListProcessorWithExclusionRules() {
        initFixtures(true)
        val filesToSelect =
            listOf<VirtualFile>(
                    myFixture.findFileInTempDir("/target"),
                    myFixture.findFileInTempDir("/out"),
                    myFixture.findFileInTempDir("/main"),
                )
                .toMutableList()

        val fileList = FileListProcessor.findFilesFromSelectedFiles(filesToSelect).toMutableList()
        val processedFiles =
            FileListProcessor.filterFiles(myFixture.findFileInTempDir(".aiignore"), fileList)

        assertNotContains(processedFiles, "App.class")
        assertNotContains(processedFiles, "Main.class")
        assertNotContains(processedFiles, "AppTest.java")
        assertContains(processedFiles, "App.java")
    }

    fun testFileListProcessorWithExclusionRulesAndOnlyOneSelection() {
        initFixtures(true)
        val filesToSelect = listOf<VirtualFile>(myFixture.findFileInTempDir("/")).toMutableList()

        val fileList = FileListProcessor.findFilesFromSelectedFiles(filesToSelect).toMutableList()
        val processedFiles =
            FileListProcessor.filterFiles(myFixture.findFileInTempDir(".aiignore"), fileList)
                .toMutableList()

        assertNotContains(processedFiles, "App.class")
        assertNotContains(processedFiles, "Main.class")
        assertNotContains(processedFiles, "AppTest.java")
        assertContains(processedFiles, "App.java")
    }

    fun testFileListProcessorWithoutExclusionRules() {
        initFixtures(false)
        val filesToSelect = listOf<VirtualFile>(myFixture.findFileInTempDir("/")).toMutableList()

        val processedFiles =
            FileListProcessor.findFilesFromSelectedFiles(filesToSelect).toMutableList()

        assertContains(processedFiles, "App.class")
        assertContains(processedFiles, "Main.class")
        assertContains(processedFiles, "AppTest.java")
        assertContains(processedFiles, "App.java")
    }

    private fun assertContains(processedFiles: MutableList<VirtualFile>, anObject: String) {
        assertTrue(
            processedFiles.stream().anyMatch { file ->
                ((file as VirtualFile).canonicalPath)?.endsWith(anObject) == true
            })
    }

    private fun assertNotContains(processedFiles: MutableList<VirtualFile>, anObject: String) {
        assertTrue(
            processedFiles.stream().noneMatch { file ->
                ((file as VirtualFile).canonicalPath)?.endsWith(anObject) == true
            })
    }
}
