package org.micoli.php

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.util.Map
import junit.framework.TestCase
import org.micoli.php.service.filesystem.FileListener
import org.micoli.php.service.filesystem.WatchEvent
import org.micoli.php.service.filesystem.Watchee

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
    private var patterns: MutableMap<String, Watchee>? = null

    override fun getTestDataPath(): String = "src/test/resources/testData"

    fun testFileListenerInitialization() {
        assertFalse("FileListener should be disabled by default", fileListener.isEnabled)
        assertNotNull("BulkFileListener should not be null", fileListener.vfsListener)
        assertTrue("Patterns should be empty by default", fileListener.getPatterns().isEmpty())
    }

    fun testSetPatternsEnablesListener() {
        initializeListenerAndTriggerFileEvent(
            Map.of(
                "php",
                Watchee(mutableListOf(createMatcher("glob:**/*.php")), WatchEvent.all()),
            ),
            object : ArrayList<VirtualFile>() {},
        )

        assertTrue("FileListener should be enabled after setPatterns", fileListener.isEnabled)
        assertEquals("Patterns should be configured", patterns, fileListener.getPatterns())
    }

    fun testResetDisablesListener() {
        initializeListenerAndTriggerFileEvent(
            Map.of(
                "php",
                Watchee(mutableListOf(createMatcher("glob:**/*.php")), WatchEvent.all()),
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
            Map.of(
                "php-files",
                Watchee(mutableListOf(createMatcher("glob:**/*.php")), WatchEvent.all()),
            ),
            listOf(testFile) as MutableList,
        )

        TestCase.assertEquals("One ID should have been processed", 1, handledIds.size)
        TestCase.assertEquals(
            "The correct ID should have been processed", "php-files", handledIds[0])
        TestCase.assertEquals("One file should have been processed", 1, handledFiles.size)
        assertEquals("The correct file should have been processed", testFile, handledFiles[0])
    }

    fun testFileEventHandlingWithMatchingPatternAndEvent() {
        val testFile = myFixture.createFile("test.php", "<?php echo 'test'; ?>")

        initializeListenerAndTriggerFileEvent(
            Map.of(
                "php-files",
                Watchee(mutableListOf(createMatcher("glob:**/*.php")), setOf(WatchEvent.DELETE)),
            ),
            listOf(testFile) as MutableList,
            isContentChangeEvent = false,
        )

        TestCase.assertEquals("One ID should have been processed", 1, handledIds.size)
        TestCase.assertEquals(
            "The correct ID should have been processed", "php-files", handledIds[0])
        TestCase.assertEquals("One file should have been processed", 1, handledFiles.size)
        assertEquals("The correct file should have been processed", testFile, handledFiles[0])

        initializeListenerAndTriggerFileEvent(
            Map.of(
                "php-files",
                Watchee(mutableListOf(createMatcher("glob:**/*.php")), setOf(WatchEvent.DELETE)),
            ),
            listOf(testFile) as MutableList,
            isContentChangeEvent = true,
        )

        TestCase.assertEquals(0, handledIds.size)
        TestCase.assertEquals(0, handledFiles.size)
    }

    fun testFileEventHandlingWithNonMatchingPattern() {
        initializeListenerAndTriggerFileEvent(
            Map.of(
                "php-files",
                Watchee(mutableListOf(createMatcher("glob:**/*.php")), WatchEvent.all()),
            ),
            listOf<VirtualFile>(myFixture.createFile("test.js", "console.log('test');"))
                as MutableList,
        )

        assertTrue("No ID should have been processed", handledIds.isEmpty())
        assertTrue("No file should have been processed", handledFiles.isEmpty())
    }

    // here
    fun testMultiplePatternsAndIds() {
        val phpFile = myFixture.createFile("test.php", "<?php echo 'test'; ?>")
        val jsFile = myFixture.createFile("test.js", "console.log('test');")
        initializeListenerAndTriggerFileEvent(
            Map.of(
                "php-files",
                Watchee(mutableListOf(createMatcher("glob:**/*.php")), WatchEvent.all()),
                "js-files",
                Watchee(mutableListOf(createMatcher("glob:**/*.js")), WatchEvent.all()),
            ),
            listOf<VirtualFile>(phpFile, jsFile) as MutableList,
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
            Map.of(
                "all-files",
                Watchee(mutableListOf(createMatcher("glob:**/*")), WatchEvent.all()),
            ),
            listOf<VirtualFile>(myFixture.tempDirFixture.findOrCreateDir("testDir")) as MutableList,
        )

        assertTrue("IDs should have been processed for a directory", !handledIds.isEmpty())
        assertTrue("Files should have been processed for a directory", !handledFiles.isEmpty())
    }

    fun testNullFileEventIsIgnored() {
        val patterns: MutableMap<String, Watchee> = HashMap()
        val allMatcher = createMatcher("glob:**/*")
        patterns["all-files"] = Watchee(mutableListOf(allMatcher), WatchEvent.all())
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
        patterns: MutableMap<String, Watchee>,
        testFiles: MutableList<VirtualFile>,
        isContentChangeEvent: Boolean = true,
    ) {
        this.patterns = patterns
        this.fileListener.setPatterns(patterns)
        this.handledIds = ArrayList()
        this.handledFiles = ArrayList()

        val events: MutableList<VFileEvent> =
            testFiles
                .stream()
                .map { testFile: VirtualFile ->
                    if (isContentChangeEvent) VFileContentChangeEvent(null, testFile, 0L, 0L)
                    else VFileDeleteEvent(null, testFile)
                }
                .toList()
                .toMutableList()

        this.fileListener.vfsListener.after(events)
    }

    private fun createMatcher(syntaxAndPattern: String): PathMatcher =
        FileSystems.getDefault().getPathMatcher(syntaxAndPattern)
}
