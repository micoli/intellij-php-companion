package org.micoli.php.ui.panels;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import java.util.List;
import javax.swing.*;
import org.micoli.php.symfony.list.CommandElementDTO;
import org.micoli.php.symfony.list.CommandService;

public class CommandsPanel extends AbstractListPanel<CommandElementDTO> {
    private static final String[] COLUMN_NAMES = {"Command", "Description", "Actions"};

    public CommandsPanel(Project project) {
        super(project, COLUMN_NAMES);
    }

    @Override
    protected void configureTableColumns() {
        table.getColumnModel().getColumn(0).setMaxWidth(800);
        table.getColumnModel().getColumn(1).setMaxWidth(200);
        table.getColumnModel().getColumn(2).setCellRenderer(new ActionIconRenderer());
        table.getColumnModel().getColumn(2).setMinWidth(50);
        table.getColumnModel().getColumn(2).setMaxWidth(50);
    }

    @Override
    protected void handleActionClick(int row) {
        CommandElementDTO elementDTO = (CommandElementDTO) table.getValueAt(row, 2);
        if (elementDTO.element() instanceof Navigatable navigatable) {
            navigatable.navigate(true);
        }
    }

    @Override
    public void refresh() {
        synchronized (lock) {
            try {
                table.getEmptyText().setText("Loading CLI, please wait...");
                clearItems();

                SwingWorker<Void, CommandElementDTO> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            CommandService commandListService = CommandService.getInstance(project);
                            List<CommandElementDTO> items = commandListService.getElements();
                            if (items != null) {
                                for (CommandElementDTO item : items) {
                                    publish(item);
                                }
                            }
                        });
                        return null;
                    }

                    @Override
                    protected void process(List<CommandElementDTO> chunks) {
                        SwingUtilities.invokeLater(() -> {
                            for (CommandElementDTO item : chunks) {
                                model.addRow(new Object[] {item.command(), item.description(), item});
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
}
