package org.micoli.php.ui.panels.symfonyProfiler.messenger

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JTable
import kotlin.arrayOf
import org.micoli.php.service.SqlUtils
import org.micoli.php.symfony.profiler.models.PHPDBQuery
import org.micoli.php.ui.panels.symfonyProfiler.AbstractProfilePanel
import org.micoli.php.ui.table.DoubleCellRenderer
import org.micoli.php.ui.table.MultiLineTableCellRenderer
import org.micoli.php.ui.table.ObjectTableModel

class ProfileMessengerPanel(val project: Project) : AbstractProfilePanel() {
    lateinit var tableModel: ObjectTableModel<PHPDBQuery>
    lateinit var cardLayout: CardLayout
    lateinit var cardPanel: JBPanel<JBPanel<*>>

    override fun getMainPanel(): JBPanel<*> {
        val columnNames = arrayOf("Sequence", "Time", "Connection", "SQL")
        tableModel = object : ObjectTableModel<PHPDBQuery>(columnNames) {}
        cardLayout = CardLayout()
        cardPanel = JBPanel<JBPanel<*>>(cardLayout)
        val table =
            JBTable(tableModel).apply {
                setShowColumns(true)
                setShowGrid(true)
                isStriped = true

                columnModel.getColumn(0).apply {
                    preferredWidth = 20
                    maxWidth = 20
                    minWidth = 20
                }
                columnModel.getColumn(1).apply {
                    preferredWidth = 70
                    maxWidth = 70
                    minWidth = 70
                    cellRenderer = DoubleCellRenderer(decimals = 6)
                }
                columnModel.getColumn(2).apply {
                    preferredWidth = 70
                    maxWidth = 70
                    minWidth = 70
                }
                columnModel.getColumn(3).apply {
                    cellRenderer = MultiLineTableCellRenderer { SqlUtils.Companion.formatSql(it) }
                }
                autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
            }
        val listPanel =
            JBPanel<JBPanel<*>>(BorderLayout()).apply {
                add(JBScrollPane(table), BorderLayout.CENTER)
            }
        return listPanel
    }

    override fun refresh() {}
}
