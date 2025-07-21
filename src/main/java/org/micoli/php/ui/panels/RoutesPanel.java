package org.micoli.php.ui.panels;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import java.util.List;
import javax.swing.*;
import org.micoli.php.symfony.list.RouteElementDTO;
import org.micoli.php.symfony.list.RouteService;

public class RoutesPanel extends AbstractListPanel<RouteElementDTO> {
    private static final String[] COLUMN_NAMES = {"URI", "Method", "Actions"};

    public RoutesPanel(Project project) {
        super(project, COLUMN_NAMES);
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setMaxWidth(800);
        table.getColumnModel().getColumn(1).setMaxWidth(120);
        table.getColumnModel().getColumn(2).setCellRenderer(new ActionIconRenderer());
        table.getColumnModel().getColumn(2).setMinWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(50);
    }

    @Override
    protected void handleActionClick(int row) {
        RouteElementDTO elementDTO = (RouteElementDTO) table.getValueAt(row, 2);
        if (elementDTO.element() instanceof Navigatable navigatable) {
            navigatable.navigate(true);
        }
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
                                model.addRow(new Object[] {item.uri(), item.methods(), item});
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
}
