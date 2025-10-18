package org.micoli.php.ui

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import kotlin.collections.MutableList
import org.micoli.php.configuration.models.Configuration
import org.micoli.php.events.ConfigurationEvents
import org.micoli.php.symfony.profiler.ProfilerEvents
import org.micoli.php.symfony.profiler.SymfonyProfileDTO
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.ui.panels.SymfonyProfilesPanel
import org.micoli.php.ui.panels.symfonyProfiler.SymfonyProfilePanel

class SymfonyWindowContent(project: Project) {
    val contentPanel: JBSplitter = JBSplitter(false, 0.40f)
    private val symfonyProfilersPanel = SymfonyProfilesPanel(project, this)
    private val symfonyProfilePanel = SymfonyProfilePanel(project)
    private var isAutoRefresh = false
    val titleActions: MutableList<AnAction?> = ArrayList()
    private val refreshLock: Any = Any()

    init {
        contentPanel.setFirstComponent(symfonyProfilersPanel)
        contentPanel.setSecondComponent(symfonyProfilePanel)

        titleActions.add(
            object :
                ToggleAction(
                    "Automatically Refresh",
                    "Automatically refresh on change",
                    PhpCompanionIcon.AutoRefresh) {
                var isActive: Boolean = isAutoRefresh

                override fun getActionUpdateThread(): ActionUpdateThread {
                    return ActionUpdateThread.BGT
                }

                override fun isSelected(anActionEvent: AnActionEvent): Boolean {
                    return isActive
                }

                override fun setSelected(anActionEvent: AnActionEvent, b: Boolean) {
                    isActive = !isActive
                    isAutoRefresh = isActive
                    symfonyProfilersPanel.isAutoRefresh = isActive
                }
            })
        titleActions.add(
            object : AnAction("Clean All Dumps", "Clean profiles", PhpCompanionIcon.Clean) {
                override fun actionPerformed(e: AnActionEvent) {
                    SymfonyProfileService.getInstance(project).cleanProfiles()
                }
            })
        titleActions.add(
            object : AnAction("Refresh", "Refresh", PhpCompanionIcon.Refresh) {
                override fun actionPerformed(e: AnActionEvent) {
                    refreshTabs()
                }
            })

        project.messageBus
            .connect()
            .subscribe(
                ConfigurationEvents.CONFIGURATION_UPDATED,
                object : ConfigurationEvents {
                    override fun configurationLoaded(loadedConfiguration: Configuration) {
                        refreshTabs()
                    }
                })
        project.messageBus
            .connect()
            .subscribe(
                ProfilerEvents.INDEX_UPDATED,
                object : ProfilerEvents {
                    override fun indexUpdated() {
                        refreshTabs()
                    }
                })
    }

    private fun refreshTabs() {
        synchronized(refreshLock) { symfonyProfilersPanel.refresh() }
    }

    fun profileSelected(symfonyProfileDTO: SymfonyProfileDTO) {
        symfonyProfilePanel.setProfile(symfonyProfileDTO)
    }
}
