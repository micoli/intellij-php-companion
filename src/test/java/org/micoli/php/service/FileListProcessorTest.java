package org.micoli.php.service;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileListProcessorTest extends BasePlatformTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    protected String getTestDataPath() {
        return "src/test/testMarkDownExporterData";
    }

    private void initFixtures(boolean withExclusionRules) {
        if (withExclusionRules) {
            myFixture.addFileToProject(".aiignore", """
                    # Ignore test file
                    src/target/**
                    src/out/**
                    **/*Test.java
                    """);
        }

        myFixture.addFileToProject("/target/classes/App.class", "compiled content");
        myFixture.addFileToProject("/out/production/Main.class", "compiled content");
        myFixture.addFileToProject("/main/App.java", "source content");
        myFixture.addFileToProject("/main/AppTest.java", "source content");
    }

    @Test
    public void testBasicFileListProcessor() {
        myFixture.copyDirectoryToProject(".", ".");
        VirtualFile[] filesToSelect = { myFixture.findFileInTempDir("/"), };

        List<VirtualFile> processedFiles = FileListProcessor.processSelectedFiles(null, filesToSelect);

        assertEquals(4, processedFiles.size());
    }

    @Test
    public void testFileListProcessorWithExclusionRules() {
        initFixtures(true);
        VirtualFile[] filesToSelect = { myFixture.findFileInTempDir("/target"), myFixture.findFileInTempDir("/out"), myFixture.findFileInTempDir("/main"), };

        List<VirtualFile> processedFiles = FileListProcessor.processSelectedFiles(myFixture.findFileInTempDir(".aiignore"), filesToSelect);

        assertNotContains(processedFiles, "App.class");
        assertNotContains(processedFiles, "Main.class");
        assertNotContains(processedFiles, "AppTest.java");
        assertContains(processedFiles, "App.java");
    }

    @Test
    public void testFileListProcessorWithExclusionRulesAndOnlyOneSelection() {
        initFixtures(true);
        VirtualFile[] filesToSelect = { myFixture.findFileInTempDir("/"), };

        List<VirtualFile> processedFiles = FileListProcessor.processSelectedFiles(myFixture.findFileInTempDir(".aiignore"), filesToSelect);

        assertNotContains(processedFiles, "App.class");
        assertNotContains(processedFiles, "Main.class");
        assertNotContains(processedFiles, "AppTest.java");
        assertContains(processedFiles, "App.java");
    }

    @Test
    public void testFileListProcessorWithoutExclusionRules() {
        initFixtures(false);
        VirtualFile[] filesToSelect = { myFixture.findFileInTempDir("/"), };

        List<VirtualFile> processedFiles = FileListProcessor.processSelectedFiles(null, filesToSelect);

        assertContains(processedFiles, "App.class");
        assertContains(processedFiles, "Main.class");
        assertContains(processedFiles, "AppTest.java");
        assertContains(processedFiles, "App.java");
    }

    private static void assertContains(List<VirtualFile> processedFiles, String anObject) {
        assertTrue(Arrays.stream(processedFiles.toArray()).anyMatch(file -> Objects.requireNonNull(((VirtualFile) file).getCanonicalPath()).endsWith(anObject)));
    }

    private static void assertNotContains(List<VirtualFile> processedFiles, String anObject) {
        assertTrue(Arrays.stream(processedFiles.toArray()).noneMatch(file -> Objects.requireNonNull(((VirtualFile) file).getCanonicalPath()).endsWith(anObject)));
    }
}
