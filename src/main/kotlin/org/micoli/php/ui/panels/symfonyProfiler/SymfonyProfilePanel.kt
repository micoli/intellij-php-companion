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
import org.micoli.php.ui.panels.symfonyProfiler.messenger.ProfileMessengerPanel

class SymfonyProfilePanel(val project: Project) : JPanel(BorderLayout(0, 0)) {
    private val tabs: JBTabs = JBTabsFactory.createTabs(project)
    private val detailPanel = ProfileDetailPanel(project)
    private val databasePanel = ProfileDBPanel(project)
    private val messengerPanel = ProfileMessengerPanel(project)
    private var symfonyProfileDTO: SymfonyProfileDTO? = null

    init {
        add(tabs.component, BorderLayout.CENTER)
        addTabPanel("Detail", detailPanel)
        addTabPanel("Database", databasePanel)
        addTabPanel("Messenger", messengerPanel)
        tabs.addListener(
            object : TabsListener {
                override fun selectionChanged(oldSelection: TabInfo?, newSelection: TabInfo?) {
                    (newSelection?.component as AbstractProfilePanel?)?.loadPanel()
                }
            })
    }

    private fun addTabPanel(title: String, panel: AbstractProfilePanel) {
        val detailTabInfo = TabInfo(panel)
        detailTabInfo.setText(title)
        tabs.addTab(detailTabInfo)
    }

    fun setProfile(symfonyProfileDTO: SymfonyProfileDTO) {
        this.symfonyProfileDTO = symfonyProfileDTO
        for (tab in tabs.tabs) {
            (tab.component as AbstractProfilePanel).updateSymfonyProfileDTO(symfonyProfileDTO)
        }
        (tabs.getSelectedInfo()?.component as AbstractProfilePanel?)?.loadPanel()
    }
}
