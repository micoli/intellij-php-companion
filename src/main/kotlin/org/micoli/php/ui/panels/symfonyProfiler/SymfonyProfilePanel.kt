package org.micoli.php.ui.panels.symfonyProfiler

import com.intellij.openapi.project.Project
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.JBTabsFactory
import com.intellij.ui.tabs.JBTabsPosition
import com.intellij.ui.tabs.TabInfo
import java.awt.BorderLayout
import javax.swing.JPanel
import org.micoli.php.symfony.profiler.SymfonyProfileDTO
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.ui.panels.symfonyProfiler.db.ProfileDBPanel

class SymfonyProfilePanel(val project: Project) : JPanel() {
    private val tabs: JBTabs = JBTabsFactory.createTabs(project)
    private val detailPanel = ProfileDetailPanel()
    private val databasePanel = ProfileDBPanel(project)
    private var symfonyProfileDTO: SymfonyProfileDTO? = null

    init {
        setLayout(BorderLayout(0, 0))
        add(tabs.component, BorderLayout.CENTER)

        val detailTabInfo = TabInfo(detailPanel)
        detailTabInfo.setText("Detail")
        tabs.addTab(detailTabInfo)

        val databaseTabInfo = TabInfo(databasePanel)
        databaseTabInfo.setText("Database")
        tabs.addTab(databaseTabInfo)
        tabs.presentation.setTabsPosition(JBTabsPosition.top)
    }

    fun setProfile(symfonyProfileDTO: SymfonyProfileDTO) {
        this.symfonyProfileDTO = symfonyProfileDTO
        val dto =
            SymfonyProfileService.getInstance(project).loadProfilerDump(symfonyProfileDTO.token)
        detailPanel.setSymfonyProfile(symfonyProfileDTO)
        databasePanel.setQueries(dto?.data?.db?.data)
    }
}
