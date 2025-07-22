package org.micoli.php.ui.panels;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractListPanel<T> extends JPanel {
    protected static final Logger LOGGER = Logger.getInstance(AbstractListPanel.class);
    protected final DefaultTableModel model;
    protected TableRowSorter<DefaultTableModel> sorter;
    protected JBTable table;
    protected SearchTextField searchField;
    protected final Project project;
    protected final Object lock = new Object();

    protected AbstractListPanel(Project project, String[] columnNames) {
        this.project = project;
        this.model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        initializeComponents();
        setupLayout();
        setupListeners();
    }

    private void initializeComponents() {
        table = new JBTable();
        table.setModel(model);
        table.setShowGrid(false);
        table.setStriped(true);
        searchField = new SearchTextField();

        table.setRowSorter(getSorter());
    }

    protected abstract TableRowSorter<DefaultTableModel> getSorter();

    private void setupLayout() {
        JBScrollPane scrollPane = new JBScrollPane(table);
        scrollPane.setBorder(JBUI.Borders.empty());
        setLayout(new BorderLayout());
        add(searchField, BorderLayout.NORTH);
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
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        try {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
        } catch (PatternSyntaxException ignored) {
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return table.getRowCount() == 0;
        }
    }

    protected abstract void refresh();
}
