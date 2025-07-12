package org.micoli.php;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhpCompanionProjectStartup implements ProjectActivity {
    private static final Logger LOG = Logger.getInstance(PhpCompanionProjectStartup.class);

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        LOG.info("Starting PhpCompanionProjectService for project: " + project.getName());
        PhpCompanionProjectService.getInstance(project);
        return Unit.INSTANCE;
    }
}
