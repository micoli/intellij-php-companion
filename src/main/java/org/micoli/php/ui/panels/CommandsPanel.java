package org.micoli.php.ui.panels;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
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
import java.util.List;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.symfony.list.CommandElementDTO;
import org.micoli.php.symfony.list.CommandService;

public class CommandsPanel extends JPanel {
    private static final Logger LOGGER = Logger.getInstance(CommandsPanel.class);
    private static final String[] COLUMN_NAMES = {"Command", "Description", "Actions"};
    private final DefaultTableModel model;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JBTable table;
    private final SearchTextField searchField;
    private final Project project;
    private final Object lock = new Object();

    public CommandsPanel(Project project) {
        this.model = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.project = project;
        table = new JBTable();
        table.setModel(model);
        table.setShowGrid(false);
        table.setStriped(true);
        searchField = new SearchTextField();
        searchField.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                updateFilter(searchField.getText());
            }
        });

        JBScrollPane scrollPane = new JBScrollPane(table);
        scrollPane.setBorder(JBUI.Borders.empty());
        this.setLayout(new BorderLayout());
        this.add(searchField, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        table.getColumnModel().getColumn(0).setMaxWidth(800);
        table.getColumnModel().getColumn(1).setMaxWidth(200);
        table.getColumnModel().getColumn(2).setCellRenderer(new ActionIconRenderer());
        table.getColumnModel().getColumn(2).setMinWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(50);

        sorter = new TableRowSorter<>(model);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        sorter.setComparator(0, String.CASE_INSENSITIVE_ORDER);
        sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER);
        sorter.setComparator(2, (o1, o2) -> 0);

        table.setRowSorter(sorter);

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
        setFocusable(true);
    }

    private void handleActionClick(int row) {
        CommandElementDTO routeElementDTO = (CommandElementDTO) table.getValueAt(row, 2);
        if (routeElementDTO.element() instanceof Navigatable navigatable) {
            navigatable.navigate(true);
        }
    }

    public void clearCommands() {
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    public void refreshCommands() {
        synchronized (lock) {
            try {
                table.getEmptyText().setText("Loading CLI, please wait...");
                clearCommands();

                SwingWorker<Void, CommandElementDTO> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            CommandService commandListService = CommandService.getInstance(project);
                            List<CommandElementDTO> routes = commandListService.getCommands();
                            if (routes != null) {
                                for (CommandElementDTO route : routes) {
                                    publish(route);
                                }
                            }
                        });
                        return null;
                    }

                    @Override
                    protected void process(List<CommandElementDTO> chunks) {
                        SwingUtilities.invokeLater(() -> {
                            for (CommandElementDTO route : chunks) {
                                model.addRow(new Object[] {route.command(), route.description(), route});
                            }
                        });
                    }

                    @Override
                    protected void done() {
                        SwingUtilities.invokeLater(() -> {
                            table.getEmptyText().setText("Nothing to show");
                            model.fireTableDataChanged();
                        });
                    }
                };
                worker.execute();

            } catch (Exception e) {
                LOGGER.error("Error refreshing CLI table", e);
            }
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
}
