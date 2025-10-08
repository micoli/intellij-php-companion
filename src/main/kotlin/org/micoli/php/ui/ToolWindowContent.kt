package org.micoli.php.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.JBTabsFactory
import com.intellij.ui.tabs.TabInfo
import java.awt.BorderLayout
import java.util.function.Consumer
import java.util.function.Function
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import org.micoli.php.PhpCompanionProjectService
import org.micoli.php.configuration.models.Configuration
import org.micoli.php.configuration.models.DisactivableConfiguration
import org.micoli.php.events.ConfigurationEvents
import org.micoli.php.events.IndexingEvents
import org.micoli.php.ui.panels.*

internal class ToolWindowContent(project: Project) {
    private class PanelRefresher(
        val aPanel: Class<*>,
        val configurationRefresher: Function<Configuration, DisactivableConfiguration?>,
    )

    val contentPanel: JPanel = JPanel()
    private val tabActions: DefaultActionGroup = DefaultActionGroup()
    val titleActions: MutableList<AnAction?> = ArrayList()
    private val tabs: JBTabs = JBTabsFactory.createTabs(project)
    private val tabMap: MutableMap<Class<*>, TabInfo> = HashMap<Class<*>, TabInfo>()
    private val panelMap: MutableMap<Class<*>, JPanel> = HashMap<Class<*>, JPanel>()
    private var configuration: Configuration? = null
    private val refresherList: MutableList<PanelRefresher?> = ArrayList<PanelRefresher?>()

    init {
        val mainPanel: JComponent = JPanel()
        this.contentPanel.setLayout(BorderLayout(2, 2))
        this.contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0))
        this.contentPanel.add(mainPanel, BorderLayout.CENTER)
        mainPanel.setLayout(BorderLayout())

        val tabbedPane = JBTabbedPane(SwingConstants.TOP, JBTabbedPane.WRAP_TAB_LAYOUT)
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0))

        titleActions.add(
            object : AnAction("Refresh", "Refresh", PhpCompanionIcon.Refresh) {
                override fun actionPerformed(e: AnActionEvent) {
                    SwingUtilities.invokeLater {
                        PhpCompanionProjectService.getInstance(project).loadConfiguration(true)
                    }
                }
            })
        addTab(ActionTreePanel(project), "Actions") { it.tasksConfiguration }
        addTab(RoutesPanel(project), "Routes") { it.routesConfiguration }
        addTab(CommandsPanel(project), "CLI") { it.commandsConfiguration }
        addTab(DoctrineEntitiesPanel(project), "Entities") { it.doctrineEntitiesConfiguration }
        addTab(OpenAPIPathPanel(project), "OAS") { it.openAPIConfiguration }

        mainPanel.add(tabs.component, BorderLayout.CENTER)
        project.messageBus
            .connect()
            .subscribe(
                ConfigurationEvents.CONFIGURATION_UPDATED,
                object : ConfigurationEvents {
                    override fun configurationLoaded(loadedConfiguration: Configuration) {
                        configuration = loadedConfiguration
                        refreshTabs()
                    }
                })
        project.messageBus
            .connect()
            .subscribe<IndexingEvents>(
                IndexingEvents.INDEXING_EVENTS,
                object : IndexingEvents {
                    override fun indexingStatusChanged(isIndexing: Boolean) {
                        if (!isIndexing) {
                            refreshTabs()
                        }
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
        SwingUtilities.invokeLater {
            if (configuration == null) {
                return@invokeLater
            }
            refresherList
                .reversed()
                .forEach(
                    Consumer { panelRefresher: PanelRefresher ->
                        manageTabVisibilityAndRefresh(
                            panelRefresher.aPanel,
                            panelRefresher.configurationRefresher.apply(configuration!!),
                        )
                    })
        }
    }

    private fun addTab(
        panel: JPanel,
        tabName: String,
        configurationRefresher: Function<Configuration, DisactivableConfiguration?>,
    ) {
        val tabInfo = TabInfo(panel)
        tabInfo.setText(tabName)
        tabInfo.setActions(tabActions, "TabActions")
        tabs.addTab(tabInfo)
        tabMap[panel.javaClass] = tabInfo
        panelMap[panel.javaClass] = panel
        panelMap[panel.javaClass] = panel
        refresherList.add(PanelRefresher(panel.javaClass, configurationRefresher))
    }
}
