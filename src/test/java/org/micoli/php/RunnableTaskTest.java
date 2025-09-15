package org.micoli.php;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.micoli.php.builders.BuiltinBuilder;
import org.micoli.php.builders.ScriptBuilder;
import org.micoli.php.builders.ShellBuilder;
import org.micoli.php.tasks.runnables.RunnableTask;

public class RunnableTaskTest extends BasePlatformTestCase {
    public static int exposedVariable = 0;

    public void testRunnableScriptSucceed() {
        // Given
        exposedVariable = 0;

        // When
        new RunnableTask(
                        getProject(),
                        ScriptBuilder.create()
                                .withId("file1")
                                .withSource("org.micoli.php.RunnableTaskTest.exposedVariable++")
                                .build())
                .run();

        // Then
        assertEquals(1, exposedVariable);
    }

    public void testRunnableShellSucceed() {
        // Given
        RunnableTask runnableTask = new RunnableTask(
                getProject(),
                ShellBuilder.create().withId("file1").withCommand("ls / ").build());

        // Then
        NullPointerException exception = org.junit.Assert.assertThrows(NullPointerException.class, runnableTask::run);
        // At least we ensure that createShellWidget is well called
        assertEquals("getOrInitToolWindow", exception.getStackTrace()[1].getMethodName());
    }

    public void testRunnableBuiltinActionSucceed() throws IOException, UnsupportedFlavorException {
        // Given
        PsiFile file = myFixture.addFileToProject("/test.txt", "ABCDEF");
        myFixture.openFileInEditor(file.getVirtualFile());
        myFixture.getEditor().getSelectionModel().setSelection(0, 3);

        // When
        new RunnableTask(
                        getProject(),
                        BuiltinBuilder.create()
                                .withId("file1")
                                .withActionId("$Copy")
                                .build())
                .run();

        // Then
        Transferable contents = CopyPasteManager.getInstance().getContents();
        assertNotNull(contents);
        assertEquals("ABC", contents.getTransferData(DataFlavor.stringFlavor));
    }
}
