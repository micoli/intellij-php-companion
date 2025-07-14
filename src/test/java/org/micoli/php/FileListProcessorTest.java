package org.micoli.php;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.micoli.php.service.FileListProcessor;

public class FileListProcessorTest extends BasePlatformTestCase {

    protected String getTestDataPath() {
        return "src/test/resources/testMarkDownExporterData";
    }

    private void initFixtures(boolean withExclusionRules) {
        if (withExclusionRules) {
            myFixture.addFileToProject(
                    "/.aiignore",
                    """
                    # Ignore test file
                    target/**
                    out/**
                    **/*Test.java
                    """);
        }

        myFixture.addFileToProject("/target/classes/App.class", "compiled content");
        myFixture.addFileToProject("/out/production/Main.class", "compiled content");
        myFixture.addFileToProject("/main/App.java", "source content");
        myFixture.addFileToProject("/main/AppTest.java", "source content");
    }

    public void testBasicFileListProcessor() {
        myFixture.copyDirectoryToProject(".", ".");
        List<VirtualFile> filesToSelect = List.of(new VirtualFile[] {
            myFixture.findFileInTempDir("/"),
        });

        List<VirtualFile> processedFiles = FileListProcessor.findFilesFromSelectedFiles(filesToSelect);

        assertEquals(4, processedFiles.size());
    }

    public void testFileListProcessorWithExclusionRules() {
        initFixtures(true);
        List<VirtualFile> filesToSelect = List.of(new VirtualFile[] {
            myFixture.findFileInTempDir("/target"),
            myFixture.findFileInTempDir("/out"),
            myFixture.findFileInTempDir("/main"),
        });

        List<VirtualFile> fileList = FileListProcessor.findFilesFromSelectedFiles(filesToSelect);
        List<VirtualFile> processedFiles =
                FileListProcessor.filterFiles(myFixture.findFileInTempDir(".aiignore"), fileList);

        assertNotContains(processedFiles, "App.class");
        assertNotContains(processedFiles, "Main.class");
        assertNotContains(processedFiles, "AppTest.java");
        assertContains(processedFiles, "App.java");
    }

    public void testFileListProcessorWithExclusionRulesAndOnlyOneSelection() {
        initFixtures(true);
        List<VirtualFile> filesToSelect = List.of(new VirtualFile[] {
            myFixture.findFileInTempDir("/"),
        });

        List<VirtualFile> fileList = FileListProcessor.findFilesFromSelectedFiles(filesToSelect);
        List<VirtualFile> processedFiles =
                FileListProcessor.filterFiles(myFixture.findFileInTempDir(".aiignore"), fileList);

        assertNotContains(processedFiles, "App.class");
        assertNotContains(processedFiles, "Main.class");
        assertNotContains(processedFiles, "AppTest.java");
        assertContains(processedFiles, "App.java");
    }

    public void testFileListProcessorWithoutExclusionRules() {
        initFixtures(false);
        List<VirtualFile> filesToSelect = List.of(new VirtualFile[] {
            myFixture.findFileInTempDir("/"),
        });

        List<VirtualFile> processedFiles = FileListProcessor.findFilesFromSelectedFiles(filesToSelect);

        assertContains(processedFiles, "App.class");
        assertContains(processedFiles, "Main.class");
        assertContains(processedFiles, "AppTest.java");
        assertContains(processedFiles, "App.java");
    }

    private static void assertContains(List<VirtualFile> processedFiles, String anObject) {
        assertTrue(Arrays.stream(processedFiles.toArray())
                .anyMatch(file -> Objects.requireNonNull(((VirtualFile) file).getCanonicalPath())
                        .endsWith(anObject)));
    }

    private static void assertNotContains(List<VirtualFile> processedFiles, String anObject) {
        assertTrue(Arrays.stream(processedFiles.toArray())
                .noneMatch(file -> Objects.requireNonNull(((VirtualFile) file).getCanonicalPath())
                        .endsWith(anObject)));
    }
}
