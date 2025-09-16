package org.micoli.php.scripting;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.micoli.php.tasks.runnables.RunnableTask;

public final class Core {
    private static final Logger LOG = Logger.getInstance(Core.class.getSimpleName());
    private final Project project;

    public Core(Project project) {
        this.project = project;
    }

    public void runAction(String actionId) {
        RunnableTask.runBuiltinAction(project, actionId, false);
    }

    public void runActionInEditor(String actionId) {
        RunnableTask.runBuiltinAction(project, actionId, true);
    }
}
