package org.micoli.php.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.JBTabsFactory;
import com.intellij.ui.tabs.TabInfo;
import java.awt.*;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.events.ConfigurationEvents;
import org.micoli.php.events.IndexingEvents;
import org.micoli.php.ui.panels.CommandsPanel;
import org.micoli.php.ui.panels.DoctrineEntitiesPanel;
import org.micoli.php.ui.panels.RoutesPanel;

class ToolWindowContent {
    private static final Logger LOGGER = Logger.getInstance(ToolWindowContent.class);
    public final JPanel contentPanel = new JPanel();
    private final JComponent mainPanel = new JPanel();
    private final RoutesPanel routesTable;
    private final CommandsPanel commandsPanel;
    private final DoctrineEntitiesPanel doctrineEntitiesPanel;
    private final JBTabbedPane tabbedPane;
    private final JBTabs tabs;
    private final DefaultActionGroup tabActions = new DefaultActionGroup();

    public ToolWindowContent(Project project) {
        this.contentPanel.setLayout(new BorderLayout(2, 2));
        this.contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.contentPanel.add(this.mainPanel, BorderLayout.CENTER);
        this.mainPanel.setLayout(new BorderLayout());

        routesTable = new RoutesPanel(project);
        commandsPanel = new CommandsPanel(project);
        doctrineEntitiesPanel = new DoctrineEntitiesPanel(project);
        tabbedPane = new JBTabbedPane(SwingConstants.TOP, JBTabbedPane.WRAP_TAB_LAYOUT);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        tabs = JBTabsFactory.createTabs(project);
        tabActions.add(new AnAction("Refresh", "Refresh", PhpCompanionIcon.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                refreshTabs();
            }
        });
        tabs.addTab(getRoutesTab());
        tabs.addTab(getCommandsTab());
        tabs.addTab(getDoctrineEntitiesTab());

        this.mainPanel.add(tabs.getComponent(), BorderLayout.CENTER);
        project.getMessageBus().connect().subscribe(ConfigurationEvents.CONFIGURATION_UPDATED, (ConfigurationEvents)
                (configuration) -> refreshTabs());
        project.getMessageBus().connect().subscribe(IndexingEvents.INDEXING_EVENTS, (IndexingEvents) (isIndexing) -> {
            if (!isIndexing) {
                refreshTabs();
            }
        });
    }

    private void refreshTabs() {
        routesTable.refresh();
        commandsPanel.refresh();
        doctrineEntitiesPanel.refresh();
    }

    private TabInfo getRoutesTab() {
        TabInfo tabInfo = new TabInfo(routesTable);
        tabInfo.setText("Routes");
        tabInfo.setActions(tabActions, "TabActions");
        return tabInfo;
    }

    private TabInfo getCommandsTab() {
        TabInfo tabInfo = new TabInfo(commandsPanel);
        tabInfo.setText("CLI");
        tabInfo.setActions(tabActions, "TabActions");
        return tabInfo;
    }

    private TabInfo getDoctrineEntitiesTab() {
        TabInfo tabInfo = new TabInfo(doctrineEntitiesPanel);
        tabInfo.setText("Entities");
        tabInfo.setActions(tabActions, "TabActions");
        return tabInfo;
    }
}
