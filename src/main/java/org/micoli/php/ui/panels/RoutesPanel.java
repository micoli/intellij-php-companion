package org.micoli.php.ui.panels;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.micoli.php.symfony.list.RouteElementDTO;
import org.micoli.php.symfony.list.RouteService;

public class RoutesPanel extends AbstractListPanel<RouteElementDTO> {
    private static final String[] COLUMN_NAMES = {"URI", "Method", "Actions"};

    public RoutesPanel(Project project) {
        super(project, "routes", COLUMN_NAMES);
    }

    @Override
    protected TableRowSorter<DefaultTableModel> getSorter() {
        sorter = new TableRowSorter<>(model);
        sorter.setSortKeys(
                List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING), new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        sorter.setComparator(
                0,
                (RouteElementDTO o1, RouteElementDTO o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.uri(), o2.uri()));
        sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER);
        sorter.setComparator(2, (o1, o2) -> 0);
        return sorter;
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setMaxWidth(800);
        table.getColumnModel().getColumn(1).setMaxWidth(120);
        table.getColumnModel().getColumn(2).setCellRenderer(new ActionIconRenderer());
        table.getColumnModel().getColumn(2).setMinWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(50);
        int baseRowHeight = table.getRowHeight();
        table.setRowHeight(baseRowHeight * 2);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            private final JLabel jLabel = new JLabel();

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                RouteElementDTO elementDTO = (RouteElementDTO) value;
                jLabel.setText(
                        value != null
                                ? "<html><div>" + elementDTO.uri() + "<br><small color=\"#777\">" + elementDTO.fqcn()
                                        + "</small></div></html>"
                                : "");

                jLabel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                jLabel.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                jLabel.setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);

                return jLabel;
            }
        });
    }

    @Override
    protected void handleActionClick(int row) {
        ApplicationManager.getApplication().invokeLater(() -> {
            RouteElementDTO elementDTO = (RouteElementDTO) table.getValueAt(row, getColumnCount() - 1);
            if (elementDTO.element() instanceof Navigatable navigatable) {
                navigatable.navigate(true);
            }
        });
    }

    @Override
    public void refresh() {
        synchronized (lock) {
            try {
                table.getEmptyText().setText("Loading routes, please wait...");
                clearItems();

                SwingWorker<Void, RouteElementDTO> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            RouteService routeListService = RouteService.getInstance(project);
                            List<RouteElementDTO> items = routeListService.getElements();
                            if (items != null) {
                                for (RouteElementDTO item : items) {
                                    publish(item);
                                }
                            }
                        });
                        return null;
                    }

                    @Override
                    protected void process(List<RouteElementDTO> chunks) {
                        SwingUtilities.invokeLater(() -> {
                            for (RouteElementDTO item : chunks) {
                                model.addRow(new Object[] {item, item.methods(), item});
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
                LOGGER.error("Error refreshing routes table", e);
            }
        }
    }

    @Override
    protected int getColumnCount() {
        return COLUMN_NAMES.length;
    }
}
