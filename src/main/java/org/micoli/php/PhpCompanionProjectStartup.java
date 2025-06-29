package org.micoli.php;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class PhpCompanionProjectStartup implements StartupActivity {
    private static final Logger LOG = Logger.getInstance(PhpCompanionProjectStartup.class);

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.info("Starting PhpCompanionProjectService for project: " + project.getName());

        PhpCompanionProjectService service = PhpCompanionProjectService.getInstance(project);
    }
}
