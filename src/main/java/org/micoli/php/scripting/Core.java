package org.micoli.php.scripting;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.micoli.php.tasks.runnables.RunnableTask;

/**
 * known as `core` in scripting engine
 */
public final class Core {
    private static final Logger LOG = Logger.getInstance(Core.class.getSimpleName());
    private final Project project;

    public Core(Project project) {
        this.project = project;
    }

    /**
     * Runs a registered action.
     *
     * @param actionId the ID of the action to run
     */
    public void runAction(String actionId) {
        RunnableTask.runBuiltinAction(project, actionId, false);
    }

    /**
     * Activates the currently opened editor and runs a registered action.
     *
     * @param actionId the ID of the action to run
     */
    public void runActionInEditor(String actionId) {
        RunnableTask.runBuiltinAction(project, actionId, true);
    }
}
