package org.micoli.php.ui.panels;

import com.intellij.find.FindModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.usages.UsageInfo2UsageAdapter;
import java.time.Duration;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.micoli.php.openAPI.OpenAPIPathElementDTO;
import org.micoli.php.openAPI.OpenAPIService;
import org.micoli.php.service.ConcurrentSearchManager;
import org.micoli.php.service.PsiElementUtil;
import org.micoli.php.service.SearchWithCompletionIndicator;
import org.micoli.php.ui.Notification;
import org.micoli.php.ui.popup.FileExtract;
import org.micoli.php.ui.popup.NavigableItem;

public class OpenAPIPathPanel extends AbstractListPanel<OpenAPIPathElementDTO> {
    private static final String[] COLUMN_NAMES = {"Uri", "Method", "Description", "Actions"};
    ConcurrentSearchManager concurrentSearchManager = new ConcurrentSearchManager(Duration.ofSeconds(20));

    public OpenAPIPathPanel(Project project) {
        super(project, "openAPI", COLUMN_NAMES);
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
        table.getColumnModel().getColumn(2).setMaxWidth(200);
        table.getColumnModel().getColumn(3).setCellRenderer(new ActionIconRenderer());
        table.getColumnModel().getColumn(3).setMinWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(50);
    }

    @Override
    protected void handleActionClick(int row) {
        OpenAPIPathElementDTO elementDTO = (OpenAPIPathElementDTO) table.getValueAt(row, 3);
        if (elementDTO == null) return;
        if (elementDTO.operationId() != null) {
            searchOperationIdDeclaration("operationId: " + elementDTO.operationId());
        }
    }

    @Override
    public void refresh() {
        synchronized (lock) {
            try {
                table.getEmptyText().setText("Loading OpenAPIPaths, please wait...");
                clearItems();

                SwingWorker<Void, OpenAPIPathElementDTO> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            OpenAPIService doctrineOpenAPIPathsService = OpenAPIService.getInstance(project);
                            List<OpenAPIPathElementDTO> items = doctrineOpenAPIPathsService.getElements();
                            if (items != null) {
                                for (OpenAPIPathElementDTO item : items) {
                                    publish(item);
                                }
                            }
                        });
                        return null;
                    }

                    @Override
                    protected void process(List<OpenAPIPathElementDTO> chunks) {
                        SwingUtilities.invokeLater(() -> {
                            for (OpenAPIPathElementDTO item : chunks) {
                                model.addRow(new Object[] {item.uri(), item.method(), item.description(), item});
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
                LOGGER.error("Error refreshing OpenAPIPaths table", e);
            }
        }
    }

    private void searchOperationIdDeclaration(String searchText) {
        if (concurrentSearchManager.isSearchInProgress(searchText)) {
            Notification.messageWithTimeout("Search already in progress", 1000);
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            concurrentSearchManager.addSearch(searchText);
            FindModel findModel = new FindModel();

            findModel.setGlobal(true);
            findModel.setProjectScope(true);
            findModel.setRegularExpressions(true);
            findModel.setWithSubdirectories(true);
            findModel.setFileFilter(null);
            findModel.setStringToFind(searchText);
            findModel.setFileFilter("*.yaml,*.yml");

            SearchWithCompletionIndicator.findUsagesWithProgress(findModel, project, 1500, results -> {
                concurrentSearchManager.removeSearch(searchText);
                if (results == null || results.isEmpty()) {
                    Notification.messageWithTimeout("No OperationId found", 1500);
                    return;
                }
                if (results.size() > 1) {
                    Notification.messageWithTimeout("Too many OperationId declarations found", 1500);
                    return;
                }
                List<NavigableItem> list = results.stream()
                        .map(usageInfo -> ApplicationManager.getApplication()
                                .runReadAction((Computable<NavigableItem>) () -> {
                                    PsiFile file = usageInfo.getFile();
                                    if (file == null) {
                                        return null;
                                    }

                                    FileExtract fileExtract = PsiElementUtil.getFileExtract(usageInfo, 1);
                                    return new NavigableItem(
                                            fileExtract, new UsageInfo2UsageAdapter(usageInfo), usageInfo.getIcon());
                                }))
                        .toList();
                list.getFirst().navigate(true);
            });
        });
    }
}
