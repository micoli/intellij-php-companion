package org.micoli.php.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

final class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ToolWindowContent toolWindowContent = new ToolWindowContent(project);
        toolWindow
                .getContentManager()
                .addContent(ContentFactory.getInstance().createContent(toolWindowContent.contentPanel, "", false));
    }
}
