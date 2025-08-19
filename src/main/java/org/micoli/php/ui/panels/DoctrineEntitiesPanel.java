package org.micoli.php.ui.panels;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.micoli.php.symfony.list.DoctrineEntityElementDTO;
import org.micoli.php.symfony.list.DoctrineEntityService;

public class DoctrineEntitiesPanel extends AbstractListPanel<DoctrineEntityElementDTO> {
    private static final String[] COLUMN_NAMES = {"Entity", "Table", "Schema", "Actions"};

    public DoctrineEntitiesPanel(Project project) {
        super(project, COLUMN_NAMES);
    }

    @Override
    protected TableRowSorter<DefaultTableModel> getSorter() {
        sorter = new TableRowSorter<>(model);
        sorter.setSortKeys(
                List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING), new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        sorter.setComparator(0, String.CASE_INSENSITIVE_ORDER);
        sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER);
        sorter.setComparator(2, String.CASE_INSENSITIVE_ORDER);
        sorter.setComparator(3, (o1, o2) -> 0);
        return sorter;
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setMaxWidth(600);
        table.getColumnModel().getColumn(1).setMaxWidth(200);
        table.getColumnModel().getColumn(2).setMaxWidth(100);
        table.getColumnModel().getColumn(3).setCellRenderer(new ActionIconRenderer());
        table.getColumnModel().getColumn(3).setMinWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(50);
    }

    @Override
    protected void handleActionClick(int row) {
        DoctrineEntityElementDTO elementDTO = (DoctrineEntityElementDTO) table.getValueAt(row, 3);
        if (elementDTO.element() instanceof Navigatable navigatable) {
            navigatable.navigate(true);
        }
    }

    @Override
    public void refresh() {
        synchronized (lock) {
            try {
                table.getEmptyText().setText("Loading Entities, please wait...");
                clearItems();

                SwingWorker<Void, DoctrineEntityElementDTO> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            DoctrineEntityService doctrineEntitiesService = DoctrineEntityService.getInstance(project);
                            List<DoctrineEntityElementDTO> items = doctrineEntitiesService.getElements();
                            if (items != null) {
                                for (DoctrineEntityElementDTO item : items) {
                                    publish(item);
                                }
                            }
                        });
                        return null;
                    }

                    @Override
                    protected void process(List<DoctrineEntityElementDTO> chunks) {
                        SwingUtilities.invokeLater(() -> {
                            for (DoctrineEntityElementDTO item : chunks) {
                                model.addRow(new Object[] {item.className(), item.name(), item.schema(), item});
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
                LOGGER.error("Error refreshing Entities table", e);
            }
        }
    }
}
