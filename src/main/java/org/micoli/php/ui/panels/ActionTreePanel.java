package org.micoli.php.ui.panels;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import org.micoli.php.events.ConfigurationEvents;
import org.micoli.php.service.TaskScheduler;
import org.micoli.php.tasks.TasksService;
import org.micoli.php.tasks.configuration.Task;
import org.micoli.php.tasks.configuration.TasksConfiguration;
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile;
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration;
import org.micoli.php.tasks.configuration.runnableTask.Script;
import org.micoli.php.tasks.configuration.runnableTask.Shell;
import org.micoli.php.ui.components.tasks.toolbar.FileObserverToolbarButton;
import org.micoli.php.ui.components.tasks.toolbar.TaskToolbarButton;
import org.micoli.php.ui.components.tasks.tree.ActionTreeNodeConfigurator;
import org.micoli.php.ui.components.tasks.tree.PathNode;
import org.micoli.php.ui.components.tasks.tree.TreeCellRenderer;

public class ActionTreePanel extends JBPanel implements Disposable {
    private static final Logger LOGGER = Logger.getInstance(ActionTreePanel.class.getSimpleName());
    private final JComponent mainPanel = new JPanel();
    private final Project project;
    private final ActionTreeNodeConfigurator actionTreeNodeConfigurator;
    private final DefaultActionGroup leftActionGroup = new DefaultActionGroup();

    public ActionTreePanel(Project project) {
        this.setLayout(new BorderLayout(2, 2));
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(createToolbar(), BorderLayout.NORTH);
        this.project = project;
        mainPanel.setLayout(new BorderLayout());
        Tree tree = new Tree(new DefaultTreeModel(new PathNode(new Object(), "Actions")));
        tree.setCellRenderer(new TreeCellRenderer());
        JBScrollPane comp = new JBScrollPane(tree);
        comp.setBorder(JBUI.Borders.empty());
        mainPanel.add(comp, BorderLayout.CENTER);
        actionTreeNodeConfigurator = new ActionTreeNodeConfigurator(project, tree);
        project.getMessageBus().connect().subscribe(ConfigurationEvents.CONFIGURATION_UPDATED, (ConfigurationEvents)
                (configuration) -> {
                    SwingUtilities.invokeLater(() -> {
                        this.loadButtonBar(project, configuration.tasksConfiguration);
                        this.loadActionTree(configuration.tasksConfiguration);
                        refresh();
                    });
                });
    }

    public void refresh() {
        this.mainPanel.revalidate();
        TaskScheduler.scheduleLater(
                () -> {
                    LOGGER.warn("Refreshing all file observers");
                    TasksService.getInstance(project).refreshObservedFiles(true);
                },
                1000);
    }

    private void loadActionTree(TasksConfiguration configuration) {
        if (configuration == null || configuration.tree == null) {
            return;
        }
        actionTreeNodeConfigurator.configureTree(configuration.getTasksMap(), configuration.tree);
    }

    private void loadButtonBar(Project project, TasksConfiguration configuration) {
        ActionManager actionManager = ActionManager.getInstance();
        cleanupActionGroup(actionManager);

        if (configuration == null || configuration.toolbar == null) {
            return;
        }

        for (Task task : configuration.toolbar) {
            RunnableTaskConfiguration runnable = configuration.getTasksMap().get(task.taskId);
            AnAction action =
                    switch (runnable) {
                        case Shell shell -> new TaskToolbarButton(project, shell);
                        case Script script -> new TaskToolbarButton(project, script);
                        case ObservedFile observedFile -> new FileObserverToolbarButton(project, observedFile);
                        default -> throw new IllegalStateException("Unexpected value: " + runnable);
                    };

            actionManager.registerAction(
                    "PhpCompanion." + runnable.getClass().getSimpleName() + "." + runnable.id, action);
            this.leftActionGroup.add(action);
        }
    }

    private JComponent createToolbar() {
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        ActionToolbar leftToolbar =
                ActionManager.getInstance().createActionToolbar("PhpCompanionRightToolbar", leftActionGroup, true);
        leftToolbar.setTargetComponent(mainPanel);
        toolbarPanel.add(leftToolbar.getComponent(), BorderLayout.WEST);

        return toolbarPanel;
    }

    @Override
    public void dispose() {

        cleanupActionGroup(ActionManager.getInstance());
    }

    private void cleanupActionGroup(ActionManager actionManager) {
        for (AnAction action : this.leftActionGroup.getChildren(actionManager)) {
            String actionId = actionManager.getId(action);
            if (actionId != null) {
                actionManager.unregisterAction(actionId);
            }

            if (action instanceof Disposable disposableAction) {
                disposableAction.dispose();
            }
        }

        this.leftActionGroup.removeAll();
    }
}
