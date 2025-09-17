package org.micoli.php;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.micoli.php.builders.ScriptBuilder;
import org.micoli.php.tasks.runnables.RunnableTask;
import org.micoli.php.ui.Notification;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ScriptingCoreTest extends BasePlatformTestCase {
    public static int exposedVariable = 0;
    AtomicReference<String> lastError = new AtomicReference<>();
    AtomicReference<String> lastMessage = new AtomicReference<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testItRunCoreRunActionInEditor() throws IOException, UnsupportedFlavorException {
        // Given
        exposedVariable = 0;
        PsiFile file = myFixture.addFileToProject("/test.txt", "ABCDEF");
        myFixture.openFileInEditor(file.getVirtualFile());
        myFixture.getEditor().getSelectionModel().setSelection(0, 3);

        // When
        new RunnableTask(
                        getProject(),
                        ScriptBuilder.create()
                                .withId("file1")
                                .withSource(
                                        """
                                core.runActionInEditor("\\$Copy");
                                org.micoli.php.ScriptingCoreTest.exposedVariable++
                                """)
                                .build())
                .run();

        // Then
        Transferable contents = CopyPasteManager.getInstance().getContents();
        assertNotNull(contents);
        assertEquals("ABC", contents.getTransferData(DataFlavor.stringFlavor));
        assertEquals(1, exposedVariable);
    }

    public void testItReportsError() {
        try (MockedStatic<Notification> mockedStatic = Mockito.mockStatic(Notification.class)) {
            mockedStatic.when(() -> Notification.error(Mockito.anyString())).thenAnswer(invocation -> {
                lastError.set(invocation.getArgument(0));
                return null;
            });
            mockedStatic.when(() -> Notification.message(Mockito.anyString())).thenAnswer(invocation -> {
                lastMessage.set(invocation.getArgument(0));
                return null;
            });
            lastError.set(null);
            lastMessage.set(null);

            // Given
            new RunnableTask(
                            getProject(),
                            ScriptBuilder.create()
                                    .withSource("org.micoli.php.ScriptingCoreTest.unUnknownExposedVariable++")
                                    .build())
                    .run();

            // Then
            assertEquals(
                    "groovy.lang.MissingPropertyException: No such property: unUnknownExposedVariable for class:"
                            + " org.micoli.php.ScriptingCoreTest",
                    lastError.get());
        }
    }
}
