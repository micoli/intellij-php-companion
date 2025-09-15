package org.micoli.php;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.io.IOException;
import org.micoli.php.builders.ObservedFileBuilder;
import org.micoli.php.builders.PostToggleScriptBuilder;
import org.micoli.php.tasks.runnables.FileObserverTask;
import org.micoli.php.ui.components.tasks.helpers.FileObserver;

public class FileObserverTaskTest extends BasePlatformTestCase {

    String filePath;
    public static int exposedVariable = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String fileContent = """
        variable1=value
        #variable2=value
        """;
        filePath = "config.env";
        myFixture.addFileToProject(filePath, fileContent);
    }

    public void testStatusToggleOnKnownActiveVariable() {
        // Given
        FileObserverTask fileObserverTask = new FileObserverTask(
                getProject(),
                ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath(filePath)
                        .withVariableName("variable1")
                        .build());
        assertEquals(FileObserver.Status.Active, fileObserverTask.getStatus());

        // When
        fileObserverTask.run();

        // Then
        assertEquals(FileObserver.Status.Inactive, fileObserverTask.getStatus());
        assertEquals("# ", fileObserverTask.getIconAndPrefix().prefix);
        assertFileContentEquals(filePath, "#variable1=value\n#variable2=value");
    }

    public void testStatusToggleWithSpecificPrefix() {
        // Given
        String fileContent = """
        variable1=value
        """;
        String specificFilePath = "config2.env";
        myFixture.addFileToProject(specificFilePath, fileContent);
        FileObserverTask fileObserverTask = new FileObserverTask(
                getProject(),
                ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath(specificFilePath)
                        .withCommentPrefix(";")
                        .withVariableName("variable1")
                        .build());
        assertEquals(FileObserver.Status.Active, fileObserverTask.getStatus());

        // When
        fileObserverTask.run();

        // Then
        assertEquals(FileObserver.Status.Inactive, fileObserverTask.getStatus());
        assertEquals("# ", fileObserverTask.getIconAndPrefix().prefix);
        assertFileContentEquals(specificFilePath, ";variable1=value");
    }

    public void testItLaunchPostAction() {
        // Given
        FileObserverTask fileObserverTask = new FileObserverTask(
                getProject(),
                ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath(filePath)
                        .withVariableName("variable1")
                        .withPostToggle(PostToggleScriptBuilder.create()
                                .withSource("org.micoli.php.FileObserverTaskTest.exposedVariable++")
                                .build())
                        .build());
        assertEquals(FileObserver.Status.Active, fileObserverTask.getStatus());
        exposedVariable = 0;

        // When
        fileObserverTask.run();

        // Then
        assertEquals(FileObserver.Status.Inactive, fileObserverTask.getStatus());
        assertEquals(1, exposedVariable);

        // And when
        fileObserverTask.run();
        assertEquals(2, exposedVariable);
    }

    public void testItDoesNotLaunchPostActionIfActionFailed() {
        // Given
        FileObserverTask fileObserverTask = new FileObserverTask(
                getProject(),
                ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath(filePath)
                        .withVariableName("unknownVariable")
                        .withPostToggle(PostToggleScriptBuilder.create()
                                .withSource("org.micoli.php.FileObserverTaskTest.exposedVariable++")
                                .build())
                        .build());
        assertEquals(FileObserver.Status.Unknown, fileObserverTask.getStatus());
        exposedVariable = 0;

        // When
        fileObserverTask.run();

        // Then
        assertEquals(0, exposedVariable);
    }

    public void testStatusToggleOnKnownInactiveVariable() {
        // Given
        FileObserverTask fileObserverTask = new FileObserverTask(
                getProject(),
                ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath(filePath)
                        .withVariableName("variable2")
                        .build());
        assertEquals(FileObserver.Status.Inactive, fileObserverTask.getStatus());

        // When
        fileObserverTask.run();

        // Then
        assertEquals(FileObserver.Status.Active, fileObserverTask.getStatus());
        assertEquals("", fileObserverTask.getIconAndPrefix().prefix);
        assertFileContentEquals(filePath, "variable1=value\nvariable2=value");
    }

    public void testStatusToggleOnUnknownVariable() {
        // Given
        FileObserverTask fileObserverTask = new FileObserverTask(
                getProject(),
                ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath(filePath)
                        .withVariableName("unknownVariable")
                        .build());
        assertEquals(FileObserver.Status.Unknown, fileObserverTask.getStatus());

        // When
        fileObserverTask.run();

        // Then
        assertEquals(FileObserver.Status.Unknown, fileObserverTask.getStatus());
        assertEquals("? ", fileObserverTask.getIconAndPrefix().prefix);
    }

    public void testStatusToggleOnUnknownFile() {
        // Given
        FileObserverTask fileObserverTask = new FileObserverTask(
                getProject(),
                ObservedFileBuilder.create()
                        .withId("file1")
                        .withFilePath("unknown-config.env")
                        .withVariableName("variable")
                        .build());
        assertEquals(FileObserver.Status.Unknown, fileObserverTask.getStatus());

        // When
        fileObserverTask.run();

        // Then
        assertEquals(FileObserver.Status.Unknown, fileObserverTask.getStatus());
        assertEquals("? ", fileObserverTask.getIconAndPrefix().prefix);
    }

    private void assertFileContentEquals(String _filepath, String content) {
        try {
            assertEquals(
                    content,
                    VfsUtilCore.loadText(myFixture.findFileInTempDir(_filepath)).trim());
        } catch (IOException e) {
            fail("File not found: " + _filepath);
        }
    }
}
