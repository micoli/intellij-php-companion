package org.micoli.php

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.util.Map
import java.util.stream.Collectors
import junit.framework.TestCase
import org.micoli.php.service.filesystem.FileListener

class FileListenerTest : BasePlatformTestCase() {
    private var handledIds: MutableList<String> = ArrayList()
    private var handledFiles: MutableList<VirtualFile> = ArrayList()
    private var fileListener: FileListener<String> =
        FileListener<String>(
            object : FileListener.VfsHandler<String> {
                override fun vfsHandle(id: String, file: VirtualFile) {
                    handledIds.add(id)
                    handledFiles.add(file)
                }
            })
    private var patterns: MutableMap<String, MutableList<PathMatcher>>? = null

    override fun getTestDataPath(): String = "src/test/resources/testData"

    fun testFileListenerInitialization() {
        assertFalse("FileListener should be disabled by default", fileListener.isEnabled)
        assertNotNull("BulkFileListener should not be null", fileListener.vfsListener)
        assertTrue("Patterns should be empty by default", fileListener.getPatterns().isEmpty())
    }

    fun testSetPatternsEnablesListener() {
        initializeListenerAndTriggerFileEvent(
            Map.of<String, MutableList<PathMatcher>>(
                "php",
                mutableListOf<PathMatcher>(
                    FileSystems.getDefault().getPathMatcher("glob:**/*.php")),
            ),
            object : ArrayList<VirtualFile>() {},
        )

        assertTrue("FileListener should be enabled after setPatterns", fileListener.isEnabled)
        assertEquals("Patterns should be configured", patterns, fileListener.getPatterns())
    }

    fun testResetDisablesListener() {
        initializeListenerAndTriggerFileEvent(
            Map.of<String, MutableList<PathMatcher>>(
                "php",
                mutableListOf<PathMatcher>(
                    FileSystems.getDefault().getPathMatcher("glob:**/*.php")),
            ),
            object : ArrayList<VirtualFile>() {},
        )

        assertTrue("FileListener should be enabled", fileListener.isEnabled)

        fileListener.reset()

        assertFalse("FileListener should be disabled after reset", fileListener.isEnabled)
        assertTrue("Patterns should be empty after reset", fileListener.getPatterns().isEmpty())
    }

    fun testFileEventHandlingWhenDisabled() {
        val testFile = myFixture.createFile("test.php", "<?php echo 'test'; ?>")

        assertFalse("FileListener should be disabled", fileListener.isEnabled)

        fileListener.vfsListener.after(
            mutableListOf(VFileContentChangeEvent(null, testFile, 0L, 0L)))

        assertTrue("No ID should have been processed", handledIds.isEmpty())
        assertTrue("No file should have been processed", handledFiles.isEmpty())
    }

    fun testFileEventHandlingWithMatchingPattern() {
        val testFile = myFixture.createFile("test.php", "<?php echo 'test'; ?>")

        initializeListenerAndTriggerFileEvent(
            Map.of<String, MutableList<PathMatcher>>(
                "php-files",
                mutableListOf<PathMatcher>(
                    FileSystems.getDefault().getPathMatcher("glob:**/*.php")),
            ),
            listOf(testFile) as MutableList<VirtualFile>,
        )

        TestCase.assertEquals("One ID should have been processed", 1, handledIds.size)
        TestCase.assertEquals(
            "The correct ID should have been processed", "php-files", handledIds[0])
        TestCase.assertEquals("One file should have been processed", 1, handledFiles.size)
        assertEquals("The correct file should have been processed", testFile, handledFiles[0])
    }

    fun testFileEventHandlingWithNonMatchingPattern() {
        initializeListenerAndTriggerFileEvent(
            Map.of<String, MutableList<PathMatcher>>(
                "php-files",
                mutableListOf<PathMatcher>(
                    FileSystems.getDefault().getPathMatcher("glob:**/*.php")),
            ),
            listOf<VirtualFile>(myFixture.createFile("test.js", "console.log('test');"))
                as MutableList<VirtualFile>,
        )

        assertTrue("No ID should have been processed", handledIds.isEmpty())
        assertTrue("No file should have been processed", handledFiles.isEmpty())
    }

    // here
    fun testMultiplePatternsAndIds() {
        val phpFile = myFixture.createFile("test.php", "<?php echo 'test'; ?>")
        val jsFile = myFixture.createFile("test.js", "console.log('test');")
        initializeListenerAndTriggerFileEvent(
            Map.of<String, MutableList<PathMatcher>>(
                "php-files",
                mutableListOf<PathMatcher>(
                    FileSystems.getDefault().getPathMatcher("glob:**/*.php")),
                "js-files",
                mutableListOf<PathMatcher>(FileSystems.getDefault().getPathMatcher("glob:**/*.js")),
            ),
            listOf<VirtualFile>(phpFile, jsFile) as MutableList<VirtualFile>,
        )

        TestCase.assertEquals("Two IDs should have been processed", 2, handledIds.size)
        TestCase.assertEquals("Two files should have been processed", 2, handledFiles.size)
        assertTrue("The php-files ID should be present", handledIds.contains("php-files"))
        assertTrue("The js-files ID should be present", handledIds.contains("js-files"))
        assertTrue("The PHP file should be present", handledFiles.contains(phpFile))
        assertTrue("The JS file should be present", handledFiles.contains(jsFile))
    }

    @Throws(IOException::class)
    fun testDirectoryEventsAreIgnored() {
        initializeListenerAndTriggerFileEvent(
            Map.of<String, MutableList<PathMatcher>>(
                "all-files",
                mutableListOf<PathMatcher>(FileSystems.getDefault().getPathMatcher("glob:**/*")),
            ),
            listOf<VirtualFile>(myFixture.tempDirFixture.findOrCreateDir("testDir"))
                as MutableList<VirtualFile>,
        )

        assertTrue("No ID should have been processed for a directory", handledIds.isEmpty())
        assertTrue("No file should have been processed for a directory", handledFiles.isEmpty())
    }

    fun testNullFileEventIsIgnored() {
        val patterns: MutableMap<String, MutableList<PathMatcher>> = HashMap()
        val allMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*")
        patterns["all-files"] = mutableListOf(allMatcher)
        fileListener.setPatterns(patterns)

        val events =
            mutableListOf<VFileEvent>(
                object : VFileEvent(Any()) {
                    override fun getFile(): VirtualFile? = null

                    override fun getFileSystem(): VirtualFileSystem =
                        throw UnsupportedOperationException()

                    override fun isValid(): Boolean = false

                    override fun hashCode(): Int = 0

                    override fun equals(o: Any?): Boolean = false

                    override fun isFromRefresh(): Boolean = false

                    override fun isFromSave(): Boolean = false

                    override fun getPath(): String = "nonexistent"

                    override fun computePath(): String = ""
                })

        val listener = fileListener.vfsListener
        listener.after(events)

        assertTrue(
            "No ID should have been processed for event with null file", handledIds.isEmpty())
        assertTrue(
            "No file should have been processed for event with null file", handledFiles.isEmpty())
    }

    private fun initializeListenerAndTriggerFileEvent(
        patterns: MutableMap<String, MutableList<PathMatcher>>,
        testFiles: MutableList<VirtualFile>,
    ) {
        this.patterns = patterns
        this.fileListener.setPatterns(patterns)

        val events: MutableList<VFileEvent> =
            testFiles
                .stream()
                .map { testFile: VirtualFile -> VFileContentChangeEvent(null, testFile, 0L, 0L) }
                .collect(Collectors.toUnmodifiableList())

        this.fileListener.vfsListener.after(events)
    }
}
