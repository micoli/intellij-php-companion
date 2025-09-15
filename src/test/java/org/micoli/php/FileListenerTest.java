package org.micoli.php;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.service.filesystem.FileListener;

public class FileListenerTest extends BasePlatformTestCase {

    private FileListener<String> fileListener;
    private List<String> handledIds;
    private List<VirtualFile> handledFiles;
    private Map<String, List<PathMatcher>> patterns;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        handledIds = new ArrayList<>();
        handledFiles = new ArrayList<>();

        fileListener = new FileListener<>((id, file) -> {
            handledIds.add(id);
            handledFiles.add(file);
        });
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData";
    }

    public void testFileListenerInitialization() {
        assertFalse("FileListener should be disabled by default", fileListener.isEnabled());
        assertNotNull("BulkFileListener should not be null", fileListener.getVfsListener());
        assertTrue(
                "Patterns should be empty by default",
                fileListener.getPatterns().isEmpty());
    }

    public void testSetPatternsEnablesListener() {
        initializeListenerAndTriggerFileEvent(
                Map.of("php", Collections.singletonList(FileSystems.getDefault().getPathMatcher("glob:**/*.php"))),
                new ArrayList<>() {});

        assertTrue("FileListener should be enabled after setPatterns", fileListener.isEnabled());
        assertEquals("Patterns should be configured", patterns, fileListener.getPatterns());
    }

    public void testResetDisablesListener() {
        initializeListenerAndTriggerFileEvent(
                Map.of("php", Collections.singletonList(FileSystems.getDefault().getPathMatcher("glob:**/*.php"))),
                new ArrayList<>() {});

        assertTrue("FileListener should be enabled", fileListener.isEnabled());

        fileListener.reset();

        assertFalse("FileListener should be disabled after reset", fileListener.isEnabled());
        assertTrue(
                "Patterns should be empty after reset",
                fileListener.getPatterns().isEmpty());
    }

    public void testFileEventHandlingWhenDisabled() {
        VirtualFile testFile = myFixture.createFile("test.php", "<?php echo 'test'; ?>");

        assertFalse("FileListener should be disabled", fileListener.isEnabled());

        fileListener
                .getVfsListener()
                .after(Collections.singletonList(new VFileContentChangeEvent(null, testFile, 0L, 0L)));

        assertTrue("No ID should have been processed", handledIds.isEmpty());
        assertTrue("No file should have been processed", handledFiles.isEmpty());
    }

    public void testFileEventHandlingWithMatchingPattern() {
        VirtualFile testFile = myFixture.createFile("test.php", "<?php echo 'test'; ?>");

        initializeListenerAndTriggerFileEvent(
                Map.of(
                        "php-files",
                        Collections.singletonList(FileSystems.getDefault().getPathMatcher("glob:**/*.php"))),
                List.of(testFile));

        assertEquals("One ID should have been processed", 1, handledIds.size());
        assertEquals("The correct ID should have been processed", "php-files", handledIds.get(0));
        assertEquals("One file should have been processed", 1, handledFiles.size());
        assertEquals("The correct file should have been processed", testFile, handledFiles.get(0));
    }

    public void testFileEventHandlingWithNonMatchingPattern() {
        initializeListenerAndTriggerFileEvent(
                Map.of(
                        "php-files",
                        Collections.singletonList(FileSystems.getDefault().getPathMatcher("glob:**/*.php"))),
                List.of(myFixture.createFile("test.js", "console.log('test');")));

        assertTrue("No ID should have been processed", handledIds.isEmpty());
        assertTrue("No file should have been processed", handledFiles.isEmpty());
    }

    // here
    public void testMultiplePatternsAndIds() {
        VirtualFile phpFile = myFixture.createFile("test.php", "<?php echo 'test'; ?>");
        VirtualFile jsFile = myFixture.createFile("test.js", "console.log('test');");
        initializeListenerAndTriggerFileEvent(
                Map.of(
                        "php-files",
                                Collections.singletonList(
                                        FileSystems.getDefault().getPathMatcher("glob:**/*.php")),
                        "js-files",
                                Collections.singletonList(
                                        FileSystems.getDefault().getPathMatcher("glob:**/*.js"))),
                List.of(phpFile, jsFile));

        assertEquals("Two IDs should have been processed", 2, handledIds.size());
        assertEquals("Two files should have been processed", 2, handledFiles.size());
        assertTrue("The php-files ID should be present", handledIds.contains("php-files"));
        assertTrue("The js-files ID should be present", handledIds.contains("js-files"));
        assertTrue("The PHP file should be present", handledFiles.contains(phpFile));
        assertTrue("The JS file should be present", handledFiles.contains(jsFile));
    }

    public void testDirectoryEventsAreIgnored() throws IOException {

        initializeListenerAndTriggerFileEvent(
                Map.of(
                        "all-files",
                        Collections.singletonList(FileSystems.getDefault().getPathMatcher("glob:**/*"))),
                List.of(myFixture.getTempDirFixture().findOrCreateDir("testDir")));

        assertTrue("No ID should have been processed for a directory", handledIds.isEmpty());
        assertTrue("No file should have been processed for a directory", handledFiles.isEmpty());
    }

    public void testNullFileEventIsIgnored() {
        Map<String, List<PathMatcher>> patterns = new HashMap<>();
        PathMatcher allMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*");
        patterns.put("all-files", Collections.singletonList(allMatcher));
        fileListener.setPatterns(patterns);

        List<VFileEvent> events = Collections.singletonList(new VFileEvent(new Object()) {
            @Override
            public VirtualFile getFile() {
                return null;
            }

            @Override
            public @NotNull VirtualFileSystem getFileSystem() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }

            @Override
            public boolean isFromRefresh() {
                return false;
            }

            @Override
            public boolean isFromSave() {
                return false;
            }

            @Override
            public String getPath() {
                return "nonexistent";
            }

            @Override
            protected @NotNull String computePath() {
                return "";
            }
        });

        BulkFileListener listener = fileListener.getVfsListener();
        listener.after(events);

        assertTrue("No ID should have been processed for event with null file", handledIds.isEmpty());
        assertTrue("No file should have been processed for event with null file", handledFiles.isEmpty());
    }

    private void initializeListenerAndTriggerFileEvent(
            Map<String, List<PathMatcher>> patterns, List<VirtualFile> testFiles) {
        this.patterns = patterns;
        this.fileListener.setPatterns(patterns);

        List<VFileEvent> events = testFiles.stream()
                .map(testFile -> new VFileContentChangeEvent(null, testFile, 0L, 0L))
                .collect(Collectors.toUnmodifiableList());

        this.fileListener.getVfsListener().after(events);
    }
}
