package org.micoli.php.ui.panels;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.ui.PhpCompanionIcon;

public abstract class AbstractListPanel<T> extends JPanel {
    protected static final Logger LOGGER = Logger.getInstance(AbstractListPanel.class);
    protected final DefaultTableModel model;
    protected TableRowSorter<DefaultTableModel> sorter;
    protected JBTable table;
    protected JPanel searchFieldPanel;
    protected SearchTextField searchField;
    protected final Project project;
    protected final Object lock = new Object();
    protected boolean isRegexMode = false;
    private final DefaultActionGroup rightActionGroup = new DefaultActionGroup();
    private final ListRowFilter<DefaultTableModel, Object> rowFilter = new ListRowFilter<>();
    private final Border border = UIManager.getBorder("TextField.border");

    protected AbstractListPanel(Project project, String panelName, String[] columnNames) {
        this.project = project;
        this.model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        initializeComponents(panelName);
        setupLayout();
        setupListeners();
    }

    private void initializeComponents(String panelName) {
        searchFieldPanel = new JPanel();
        searchFieldPanel.setLayout(new BorderLayout());
        table = new JBTable();
        table.setModel(model);
        table.setShowGrid(false);
        table.setStriped(true);
        searchField = new SearchTextField(true, true, panelName);
        rightActionGroup.add(new ToggleAction("Regex", "Toggle regex mode", PhpCompanionIcon.Regexp) {
            boolean isRegex = isRegexMode;

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }

            @Override
            public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
                return isRegex;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent anActionEvent, boolean b) {
                isRegex = !isRegex;
                isRegexMode = isRegex;
                updateFilter(searchField.getText());
            }
        });
        ActionToolbar rightToolbar = ActionManager.getInstance()
                .createActionToolbar("PhpCompanion" + panelName + "RightToolbar", rightActionGroup, true);
        rightToolbar.setTargetComponent(this);
        searchFieldPanel.add(rightToolbar.getComponent(), BorderLayout.EAST);

        sorter = getSorter();
        sorter.setRowFilter(rowFilter);
        table.setRowSorter(sorter);
    }

    protected abstract TableRowSorter<DefaultTableModel> getSorter();

    private void setupLayout() {
        JBScrollPane scrollPane = new JBScrollPane(table);
        scrollPane.setBorder(JBUI.Borders.empty());
        setLayout(new BorderLayout());
        searchFieldPanel.add(searchField, BorderLayout.CENTER);
        add(searchFieldPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        configureTableColumns();
    }

    protected abstract void configureTableColumns();

    private void setupListeners() {
        searchField.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                updateFilter(searchField.getText());
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (e.getClickCount() == 2) {
                    handleActionClick(row);
                    return;
                }
                if (col == 2) {
                    handleActionClick(row);
                }
            }
        });

        setupKeyListeners();
        setFocusable(true);
    }

    private void setupKeyListeners() {
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleActionClick(table.getSelectedRow());
                }
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && table.getRowCount() > 0) {
                    table.requestFocusInWindow();
                    table.setRowSelectionInterval(0, 0);
                }
            }
        });
    }

    protected abstract void handleActionClick(int row);

    public void clearItems() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    public void updateFilter(String text) {
        JBTextField textEditor = searchField.getTextEditor();
        try {
            rowFilter.updateFilter(text, isRegexMode);
            sorter.sort();
            textEditor.setBorder(border);
        } catch (Exception exception) {
            textEditor.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.ORANGE), border));
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return table.getRowCount() == 0;
        }
    }

    public abstract void refresh();
}
