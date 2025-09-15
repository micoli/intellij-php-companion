package org.micoli.php.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.JBTabsFactory;
import com.intellij.ui.tabs.TabInfo;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.PhpCompanionProjectService;
import org.micoli.php.configuration.models.Configuration;
import org.micoli.php.configuration.models.DisactivableConfiguration;
import org.micoli.php.events.ConfigurationEvents;
import org.micoli.php.events.IndexingEvents;
import org.micoli.php.ui.panels.*;

class ToolWindowContent {
    private record PanelRefresher(Class<?> aPanel, Supplier<DisactivableConfiguration> configurationRefresher) {}

    private static final Logger LOGGER = Logger.getInstance(ToolWindowContent.class.getSimpleName());
    public final JPanel contentPanel = new JPanel();
    private final DefaultActionGroup tabActions = new DefaultActionGroup();
    private final JBTabs tabs;
    private final Map<Class<?>, TabInfo> tabMap = new HashMap<>();
    private final Map<Class<?>, JBPanel<?>> panelMap = new HashMap<>();
    private Configuration configuration = null;
    private final List<PanelRefresher> refresherList = new ArrayList<>();

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
                SwingUtilities.invokeLater(() -> {
                    PhpCompanionProjectService.getInstance(project).loadConfiguration(true);
                });
            }
        });
        addTab(new ActionTreePanel(project), "Actions", () -> configuration.tasksConfiguration);
        addTab(new RoutesPanel(project), "Routes", () -> configuration.routesConfiguration);
        addTab(new CommandsPanel(project), "CLI", () -> configuration.commandsConfiguration);
        addTab(new DoctrineEntitiesPanel(project), "Entities", () -> configuration.doctrineEntitiesConfiguration);
        addTab(new OpenAPIPathPanel(project), "OAS", () -> configuration.openAPIConfiguration);

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
            JBPanel<?> jbPanel = panelMap.get(_class);
            if (jbPanel instanceof AbstractListPanel<?> panel) {
                panel.refresh();
            }
        }
    }

    private void refreshTabs() {
        SwingUtilities.invokeLater(() -> {
            if (configuration == null) {
                return;
            }
            refresherList.reversed().forEach((panelRefresher) -> {
                manageTabVisibilityAndRefresh(
                        panelRefresher.aPanel,
                        panelRefresher.configurationRefresher().get());
            });
        });
    }

    private void addTab(JBPanel<?> panel, String tabName, Supplier<DisactivableConfiguration> configurationRefresher) {
        TabInfo tabInfo = new TabInfo(panel);
        tabInfo.setText(tabName);
        tabInfo.setActions(tabActions, "TabActions");
        tabs.addTab(tabInfo);
        tabMap.put(panel.getClass(), tabInfo);
        panelMap.put(panel.getClass(), panel);
        panelMap.put(panel.getClass(), panel);
        refresherList.add(new PanelRefresher(panel.getClass(), configurationRefresher));
    }
}
