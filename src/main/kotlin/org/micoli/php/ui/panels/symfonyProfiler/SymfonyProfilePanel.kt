package org.micoli.php.ui.panels.symfonyProfiler

import com.intellij.openapi.project.Project
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.JBTabsFactory
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.TabsListener
import java.awt.BorderLayout
import javax.swing.JPanel
import org.micoli.php.symfony.profiler.SymfonyProfileDTO
import org.micoli.php.ui.panels.symfonyProfiler.db.ProfileDBPanel
import org.micoli.php.ui.panels.symfonyProfiler.detail.ProfileDetailPanel
import org.micoli.php.ui.panels.symfonyProfiler.logs.ProfileLogsPanel
import org.micoli.php.ui.panels.symfonyProfiler.messenger.ProfileMessengerPanel

class SymfonyProfilePanel(val project: Project) : JPanel(BorderLayout(0, 0)) {
    private val tabs: JBTabs = JBTabsFactory.createTabs(project)

    init {
        add(tabs.component, BorderLayout.CENTER)
        addTabPanel("Detail", ProfileDetailPanel(project))
        addTabPanel("Database", ProfileDBPanel(project))
        addTabPanel("Messenger", ProfileMessengerPanel(project))
        addTabPanel("Logs", ProfileLogsPanel(project))
        tabs.addListener(
            object : TabsListener {
                override fun selectionChanged(oldSelection: TabInfo?, newSelection: TabInfo?) {
                    (newSelection?.component as AbstractProfilePanel?)?.refreshPanel()
                }
            })
    }

    private fun addTabPanel(title: String, panel: AbstractProfilePanel) {
        val detailTabInfo = TabInfo(panel)
        detailTabInfo.setText(title)
        tabs.addTab(detailTabInfo)
    }

    fun setProfile(symfonyProfileDTO: SymfonyProfileDTO) {
        for (tab in tabs.tabs) {
            (tab.component as AbstractProfilePanel).updateSymfonyProfileDTO(symfonyProfileDTO)
        }
        (tabs.selectedInfo?.component as AbstractProfilePanel?)?.refreshPanel()
    }
}
