package org.micoli.php;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.micoli.php.builders.ScriptBuilder;
import org.micoli.php.builders.ShellBuilder;
import org.micoli.php.tasks.runnables.RunnableTask;

public class RunnableTaskTest extends BasePlatformTestCase {
    public static int exposedVariable = 0;

    public void testRunnableScriptSucceed() {
        // Given
        RunnableTask runnableTask = new RunnableTask(
                getProject(),
                ScriptBuilder.create()
                        .withId("file1")
                        .withSource("org.micoli.php.RunnableTaskTest.exposedVariable++")
                        .build());
        exposedVariable = 0;

        // When
        runnableTask.run();

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
}
