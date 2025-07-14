package org.micoli.php.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.util.Consumer;
import java.awt.event.MouseEvent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService;

public final class PhpCompanionStatusBarWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {
    private final Project project;
    private StatusBar statusBar;

    public PhpCompanionStatusBarWidget(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String ID() {
        return "PHPCompanion";
    }

    @Override
    public @NotNull WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    @Override
    public @NotNull String getText() {
        return "Companion";
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Override
    public @NotNull String getTooltipText() {
        return "PHP Companion Options";
    }

    @NotNull
    public Consumer<MouseEvent> getClickConsumer() {
        return e -> {
            if (!e.isPopupTrigger() && MouseEvent.BUTTON1 == e.getButton()) {
                showPopup(e);
            }
        };
    }

    private void showPopup(MouseEvent e) {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new ToggleAction("SourceExport: Use .aiignore File", "Description 1", null) {
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return ExportSourceToMarkdownService.getUseIgnoreFile();
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                ExportSourceToMarkdownService.toggleUseIgnoreFile();
            }
        });
        group.add(new ToggleAction("SourceExport: Use Contextual Namespaces", "Description 1", null) {
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return ExportSourceToMarkdownService.getUseContextualNamespaces();
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                ExportSourceToMarkdownService.toggleUseContextualNamespaces();
            }
        });

        JBPopupFactory.getInstance()
                .createActionGroupPopup(
                        "Options",
                        group,
                        DataManager.getInstance().getDataContext(e.getComponent()),
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true)
                .showUnderneathOf(e.getComponent());
    }

    public static class Factory implements StatusBarWidgetFactory {
        @Override
        public @NotNull String getId() {
            return "PHPCompanion";
        }

        @Override
        public @Nls @NotNull String getDisplayName() {
            return "PHP Companion";
        }

        @Override
        public boolean isAvailable(@NotNull Project project) {
            return true;
        }

        @Override
        public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
            return new PhpCompanionStatusBarWidget(project);
        }

        @Override
        public void disposeWidget(@NotNull StatusBarWidget widget) {
            widget.dispose();
        }
    }
}
