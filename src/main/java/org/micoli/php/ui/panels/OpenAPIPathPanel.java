package org.micoli.php.ui.panels;

import com.intellij.find.FindModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.usages.UsageInfo2UsageAdapter;
import java.awt.*;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.micoli.php.openAPI.OpenAPIPathElementDTO;
import org.micoli.php.openAPI.OpenAPIService;
import org.micoli.php.service.intellij.psi.PsiElementUtil;
import org.micoli.php.service.intellij.search.ConcurrentSearchManager;
import org.micoli.php.service.intellij.search.SearchWithCompletionIndicator;
import org.micoli.php.ui.Notification;
import org.micoli.php.ui.popup.FileExtract;
import org.micoli.php.ui.popup.NavigableItem;

public class OpenAPIPathPanel extends AbstractListPanel<OpenAPIPathElementDTO> {
    private static final String[] COLUMN_NAMES = {"Uri", "Method", "Actions"};
    ConcurrentSearchManager concurrentSearchManager = new ConcurrentSearchManager(Duration.ofSeconds(20));

    public OpenAPIPathPanel(Project project) {
        super(project, "openAPI", COLUMN_NAMES);
    }

    @Override
    protected TableRowSorter<DefaultTableModel> getSorter() {
        sorter = new TableRowSorter<>(model);
        sorter.setSortKeys(
                List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING), new RowSorter.SortKey(1, SortOrder.ASCENDING)));
        sorter.setComparator(
                0,
                (OpenAPIPathElementDTO o1, OpenAPIPathElementDTO o2) ->
                        String.CASE_INSENSITIVE_ORDER.compare(o1.uri(), o2.uri()));
        sorter.setComparator(1, String.CASE_INSENSITIVE_ORDER);
        sorter.setComparator(2, (o1, o2) -> 0);
        return sorter;
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setMaxWidth(1600);
        table.getColumnModel().getColumn(1).setMaxWidth(90);
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
                OpenAPIPathElementDTO elementDTO = (OpenAPIPathElementDTO) value;
                jLabel.setText(
                        value != null
                                ? String.format(
                                        """
                                        <html>
                                            <div>
                                                %s<br>
                                                <small color="#777">%s <strong>(%s)</strong></small>
                                            </div>
                                        </html>
                                        """,
                                        Objects.requireNonNullElse(elementDTO.uri(), ""),
                                        Objects.requireNonNullElse(elementDTO.description(), ""),
                                        Objects.requireNonNullElse(elementDTO.operationId(), ""))
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
            OpenAPIPathElementDTO elementDTO = (OpenAPIPathElementDTO) table.getValueAt(row, getColumnCount() - 1);
            if (elementDTO == null) return;
            if (elementDTO.operationId() != null) {
                searchOperationIdDeclaration("operationId: " + elementDTO.operationId());
            }
        });
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
                                model.addRow(new Object[] {item, item.method(), item});
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
                results.stream()
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
                        .toList()
                        .forEach(c -> c.navigate(true));
            });
        });
    }

    @Override
    protected int getColumnCount() {
        return COLUMN_NAMES.length;
    }
}
