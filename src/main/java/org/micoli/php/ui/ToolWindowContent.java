package org.micoli.php.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.JBTabsFactory;
import com.intellij.ui.tabs.TabInfo;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.configuration.models.Configuration;
import org.micoli.php.configuration.models.DisactivableConfiguration;
import org.micoli.php.events.ConfigurationEvents;
import org.micoli.php.events.IndexingEvents;
import org.micoli.php.ui.panels.*;

class ToolWindowContent {
    private static final Logger LOGGER = Logger.getInstance(ToolWindowContent.class);
    public final JPanel contentPanel = new JPanel();
    private final DefaultActionGroup tabActions = new DefaultActionGroup();
    private final JBTabs tabs;
    private final Map<Class<?>, TabInfo> tabMap = new HashMap<>();
    private final Map<Class<?>, AbstractListPanel<?>> panelMap = new HashMap<>();
    private Configuration configuration = null;

    public ToolWindowContent(Project project) {
        JComponent mainPanel = new JPanel();
        this.contentPanel.setLayout(new BorderLayout(2, 2));
        this.contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.contentPanel.add(mainPanel, BorderLayout.CENTER);
        mainPanel.setLayout(new BorderLayout());

        JBTabbedPane tabbedPane = new JBTabbedPane(SwingConstants.TOP, JBTabbedPane.WRAP_TAB_LAYOUT);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        tabs = JBTabsFactory.createTabs(project);
        tabActions.add(new AnAction("Refresh", "Refresh", PhpCompanionIcon.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                refreshTabs();
            }
        });
        addTab(new RoutesPanel(project), "Routes");
        addTab(new CommandsPanel(project), "CLI");
        addTab(new DoctrineEntitiesPanel(project), "Entities");
        addTab(new OpenAPIPathPanel(project), "OAS");

        mainPanel.add(tabs.getComponent(), BorderLayout.CENTER);
        project.getMessageBus().connect().subscribe(ConfigurationEvents.CONFIGURATION_UPDATED, (ConfigurationEvents)
                (configuration) -> {
                    this.configuration = configuration;
                    refreshTabs();
                });
        project.getMessageBus().connect().subscribe(IndexingEvents.INDEXING_EVENTS, (IndexingEvents) (isIndexing) -> {
            if (!isIndexing) {
                refreshTabs();
            }
        });
    }

    private void manageTabVisibilityAndRefresh(Class<?> _class, DisactivableConfiguration configuration) {
        boolean isEnabled = (configuration != null && configuration.isEnabled());
        tabMap.get(_class).setHidden(!isEnabled);
        if (isEnabled) {
            panelMap.get(_class).refresh();
        }
    }

    private void refreshTabs() {
        SwingUtilities.invokeLater(() -> {
            manageTabVisibilityAndRefresh(RoutesPanel.class, configuration.routesConfiguration);
            manageTabVisibilityAndRefresh(CommandsPanel.class, configuration.commandsConfiguration);
            manageTabVisibilityAndRefresh(DoctrineEntitiesPanel.class, configuration.doctrineEntitiesConfiguration);
            manageTabVisibilityAndRefresh(OpenAPIPathPanel.class, configuration.openAPIConfiguration);
        });
    }

    private void addTab(AbstractListPanel<?> panel, String tabName) {
        TabInfo tabInfo = new TabInfo(panel);
        tabInfo.setText(tabName);
        tabInfo.setActions(tabActions, "TabActions");
        tabs.addTab(tabInfo);
        tabMap.put(panel.getClass(), tabInfo);
        panelMap.put(panel.getClass(), panel);
    }
}
