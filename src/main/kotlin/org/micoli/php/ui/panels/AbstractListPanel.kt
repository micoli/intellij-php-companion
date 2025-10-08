package org.micoli.php.ui.panels

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.border.Border
import javax.swing.event.DocumentEvent
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableRowSorter
import org.micoli.php.ui.PhpCompanionIcon

abstract class AbstractListPanel<T>
protected constructor(
    protected val project: Project,
    panelName: String?,
    columnNames: Array<String>
) : JPanel() {
    protected val model: DefaultTableModel
    protected lateinit var innerSorter: TableRowSorter<DefaultTableModel>
    protected lateinit var table: JBTable
    protected lateinit var searchFieldPanel: JPanel
    protected lateinit var searchField: SearchTextField
    protected val lock: Any = Any()
    protected var isRegexMode: Boolean = false
    private val rightActionGroup = DefaultActionGroup()
    private val rowFilter = ListRowFilter<DefaultTableModel?, Any?>()
    private val border: Border? = UIManager.getBorder("TextField.border")

    protected abstract fun getColumnCount(): Int

    init {
        this.model =
            object : DefaultTableModel(columnNames, 0) {
                override fun isCellEditable(row: Int, column: Int): Boolean {
                    return false
                }
            }

        initializeComponents(panelName)
        setupLayout()
        setupListeners()
    }

    private fun initializeComponents(panelName: String?) {
        searchFieldPanel = JPanel()
        searchFieldPanel.setLayout(BorderLayout())
        table = JBTable()
        table.model = model
        table.setShowGrid(false)
        table.isStriped = true
        searchField = SearchTextField(true, true, panelName)
        rightActionGroup.add(
            object : ToggleAction("Regex", "Toggle regex mode", PhpCompanionIcon.Regexp) {
                var isRegex: Boolean = isRegexMode

                override fun getActionUpdateThread(): ActionUpdateThread {
                    return ActionUpdateThread.BGT
                }

                override fun isSelected(anActionEvent: AnActionEvent): Boolean {
                    return isRegex
                }

                override fun setSelected(anActionEvent: AnActionEvent, b: Boolean) {
                    isRegex = !isRegex
                    isRegexMode = isRegex
                    updateFilter(searchField.text)
                }
            })
        val rightToolbar =
            ActionManager.getInstance()
                .createActionToolbar(
                    "PhpCompanion" + panelName + "RightToolbar", rightActionGroup, true)
        rightToolbar.targetComponent = this
        searchFieldPanel.add(rightToolbar.component, BorderLayout.EAST)

        innerSorter = getSorter()
        innerSorter.setRowFilter(rowFilter)
        table.setRowSorter(innerSorter)
    }

    protected abstract fun getSorter(): TableRowSorter<DefaultTableModel>

    private fun setupLayout() {
        val scrollPane = JBScrollPane(table)
        scrollPane.setBorder(JBUI.Borders.empty())
        setLayout(BorderLayout())
        searchFieldPanel.add(searchField, BorderLayout.CENTER)
        add(searchFieldPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)

        configureTableColumns()
    }

    protected abstract fun configureTableColumns()

    private fun setupListeners() {
        searchField.addDocumentListener(
            object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) {
                    updateFilter(searchField.text)
                }
            })

        table.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val row = table.rowAtPoint(e.getPoint())
                    val col = table.columnAtPoint(e.getPoint())
                    if (e.getClickCount() == 2) {
                        handleActionDoubleClick(row)
                        return
                    }
                    if (e.getClickCount() == 1) {
                        handleActionSingleClick(row)
                        return
                    }
                    if (col == getColumnCount() - 1) {
                        handleActionDoubleClick(row)
                    }
                }
            })

        table
            .getSelectionModel()
            .addListSelectionListener(
                object : ListSelectionListener {
                    override fun valueChanged(e: ListSelectionEvent) {
                        if (e.valueIsAdjusting) {
                            return
                        }

                        val selectedRow = table.selectedRow
                        if (selectedRow >= 0) {
                            handleActionLineSelected(selectedRow)
                        }
                    }
                })

        setupKeyListeners()
        setFocusable(true)
    }

    private fun setupKeyListeners() {
        table.addKeyListener(
            object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        handleActionDoubleClick(table.selectedRow)
                    }
                }
            })

        searchField.addKeyListener(
            object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER && table.getRowCount() > 0) {
                        table.requestFocusInWindow()
                        table.setRowSelectionInterval(0, 0)
                    }
                }
            })
    }

    protected abstract fun handleActionDoubleClick(row: Int)

    protected open fun handleActionSingleClick(row: Int) {}

    protected open fun handleActionLineSelected(row: Int) {}

    fun clearItems() {
        while (model.rowCount > 0) {
            model.removeRow(0)
        }
    }

    fun updateFilter(text: String) {
        val textEditor = searchField.textEditor
        try {
            rowFilter.updateFilter(text, isRegexMode)
            innerSorter.sort()
            textEditor.setBorder(border)
        } catch (_: Exception) {
            textEditor.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.ORANGE), border))
        }
    }

    val isEmpty: Boolean
        get() {
            synchronized(lock) {
                return table.getRowCount() == 0
            }
        }

    abstract fun refresh()

    companion object {
        protected val LOGGER: Logger =
            Logger.getInstance(AbstractListPanel::class.java.getSimpleName())
    }
}
