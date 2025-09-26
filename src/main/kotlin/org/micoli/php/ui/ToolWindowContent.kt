package org.micoli.php.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.JBTabsFactory
import javax.swing.JPanel
import org.micoli.php.PhpCompanionProjectService
import org.micoli.php.configuration.models.DisactivableConfiguration
import org.micoli.php.events.ConfigurationEvents
import org.micoli.php.events.IndexingEvents
import org.micoli.php.ui.panels.*

internal class ToolWindowContent(project: com.intellij.openapi.project.Project) {
    private class PanelRefresher(
        val aPanel: Class<*>,
        val configurationRefresher: java.util.function.Supplier<DisactivableConfiguration?>?,
    )

    val contentPanel: JPanel = JPanel()
    private val tabActions: DefaultActionGroup = DefaultActionGroup()
    private val tabs: JBTabs
    private val tabMap: MutableMap<Class<*>, com.intellij.ui.tabs.TabInfo> =
        java.util.HashMap<Class<*>, com.intellij.ui.tabs.TabInfo>()
    private val panelMap: MutableMap<Class<*>, JPanel> = java.util.HashMap<Class<*>, JPanel>()
    private var configuration: org.micoli.php.configuration.models.Configuration? = null
    private val refresherList: MutableList<PanelRefresher?> = java.util.ArrayList<PanelRefresher?>()

    init {
        val mainPanel: javax.swing.JComponent = JPanel()
        this.contentPanel.setLayout(java.awt.BorderLayout(2, 2))
        this.contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0))
        this.contentPanel.add(mainPanel, java.awt.BorderLayout.CENTER)
        mainPanel.setLayout(java.awt.BorderLayout())

        val tabbedPane = JBTabbedPane(javax.swing.SwingConstants.TOP, JBTabbedPane.WRAP_TAB_LAYOUT)
        tabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0))

        tabs = JBTabsFactory.createTabs(project)
        tabActions.add(
            object : AnAction("Refresh", "Refresh", PhpCompanionIcon.Refresh) {
                override fun actionPerformed(e: AnActionEvent) {
                    javax.swing.SwingUtilities.invokeLater {
                        PhpCompanionProjectService.getInstance(project).loadConfiguration(true)
                    }
                }
            })
        addTab(ActionTreePanel(project), "Actions") { configuration!!.tasksConfiguration }
        addTab(RoutesPanel(project), "Routes") { configuration!!.routesConfiguration }
        addTab(CommandsPanel(project), "CLI") { configuration!!.commandsConfiguration }
        addTab(DoctrineEntitiesPanel(project), "Entities") {
            configuration!!.doctrineEntitiesConfiguration
        }
        addTab(OpenAPIPathPanel(project), "OAS") { configuration!!.openAPIConfiguration }

        mainPanel.add(tabs.component, java.awt.BorderLayout.CENTER)
        project.messageBus
            .connect()
            .subscribe<ConfigurationEvents>(
                ConfigurationEvents.CONFIGURATION_UPDATED,
                ConfigurationEvents {
                    configuration: org.micoli.php.configuration.models.Configuration? ->
                    this.configuration = configuration
                    refreshTabs()
                },
            )
        project.messageBus
            .connect()
            .subscribe<IndexingEvents>(
                IndexingEvents.INDEXING_EVENTS,
                IndexingEvents { isIndexing: Boolean ->
                    if (!isIndexing) {
                        refreshTabs()
                    }
                },
            )
    }

    private fun manageTabVisibilityAndRefresh(
        classParameter: Class<*>?,
        configuration: DisactivableConfiguration?
    ) {
        val isEnabled = (configuration != null && !configuration.isDisabled())
        tabMap[classParameter]!!.isHidden = !isEnabled
        if (isEnabled) {
            val jbPanel: JPanel? = panelMap[classParameter]
            if (jbPanel is AbstractListPanel<*>) {
                jbPanel.refresh()
            }
        }
    }

    private fun refreshTabs() {
        javax.swing.SwingUtilities.invokeLater {
            if (configuration == null) {
                return@invokeLater
            }
            refresherList
                .reversed()
                .forEach(
                    java.util.function.Consumer { panelRefresher: PanelRefresher? ->
                        manageTabVisibilityAndRefresh(
                            panelRefresher!!.aPanel,
                            panelRefresher.configurationRefresher!!.get(),
                        )
                    })
        }
    }

    private fun addTab(
        panel: JPanel,
        tabName: String,
        configurationRefresher: java.util.function.Supplier<DisactivableConfiguration?>?,
    ) {
        val tabInfo = com.intellij.ui.tabs.TabInfo(panel)
        tabInfo.setText(tabName)
        tabInfo.setActions(tabActions, "TabActions")
        tabs.addTab(tabInfo)
        tabMap[panel.javaClass] = tabInfo
        panelMap[panel.javaClass] = panel
        panelMap[panel.javaClass] = panel
        refresherList.add(PanelRefresher(panel.javaClass, configurationRefresher))
    }
}
