package org.micoli.php.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import kotlin.collections.MutableList
import org.micoli.php.configuration.models.Configuration
import org.micoli.php.events.ConfigurationEvents
import org.micoli.php.symfony.profiler.SymfonyProfileDTO
import org.micoli.php.ui.panels.SymfonyProfilesPanel
import org.micoli.php.ui.panels.symfonyProfiler.SymfonyProfilePanel

class SymfonyWindowContent(project: Project) {
    val contentPanel: JBSplitter = JBSplitter(false, 0.40f)
    private val symfonyProfilersPanel: SymfonyProfilesPanel = SymfonyProfilesPanel(project, this)
    private val symfonyProfilePanel: SymfonyProfilePanel = SymfonyProfilePanel(project)
    val titleActions: MutableList<AnAction?> = ArrayList()

    init {
        contentPanel.setFirstComponent(symfonyProfilersPanel)
        contentPanel.setSecondComponent(symfonyProfilePanel)

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
    }

    private fun refreshTabs() {
        symfonyProfilersPanel.refresh()
    }

    fun profileSelected(symfonyProfileDTO: SymfonyProfileDTO) {
        symfonyProfilePanel.setProfile(symfonyProfileDTO)
    }
}
