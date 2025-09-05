package org.micoli.php;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.micoli.php.symfony.list.SearchableRecord;
import org.micoli.php.ui.panels.ListRowFilter;

@RunWith(Parameterized.class)
public class ListRowFilterTest {
    private final String searchText;
    private final boolean isRegexMode;
    private final String expectedResult;

    public record TestElementDTO(@NotNull String uri, @NotNull String name) implements SearchableRecord {
        @Override
        public List<@NotNull String> getSearchString() {
            return List.of(uri, name);
        }
    }

    public ListRowFilterTest(String searchText, boolean isRegexMode, String expectedResult) {
        this.searchText = searchText;
        this.isRegexMode = isRegexMode;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "Filter with \"{0}\" and isRegex is {1}")
    public static Collection<Object[]> data() throws IOException {
        return Arrays.asList(new Object[][] {
            {"", false, "t1,t2,t3,t4,t5,t6,t7"},
            {"", true, "t1,t2,t3,t4,t5,t6,t7"},
            {"good", false, "t1"},
            {"description", false, "t1,t2"},
            {"test", false, "t1,t2,t3,t4"},
            {"nonexistent", false, ""},
            {"/test/.*", false, ""},
            {"/test/.*toto", true, "t1,t2,t3"},
            {"/test/.*", true, "t1,t2,t3,t4"},
            {".*\\{id\\}.*", true, "t1,t2,t3"},
            {"^/actor.*", true, "t1,t2"},
            {"resource.*delete", true, "t1"},
            {"acToR update", false, "t1"},
            {"ACTOR", false, "t1,t2"},
            {"Update", false, "t1"},
            {"/{id}/", false, "t1,t2,t3"},
            {"toto", false, "t1,t2,t3"},
            {".*toto$", true, "t1,t2,t3"},
        });
    }

    @Test
    public void testItFiltersTableRows() {
        String[] columnNames = {"Name", "Element"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        model.addRow(new Object[] {"t1", new TestElementDTO("/test/toto", "A good description")});
        model.addRow(new Object[] {"t2", new TestElementDTO("/test/toto", "A bad description")});
        model.addRow(new Object[] {"t3", new TestElementDTO("/test/tata/toto", "Without desc")});
        model.addRow(new Object[] {"t4", new TestElementDTO("/test/tata/tutu", "Without desc")});
        model.addRow(new Object[] {"t5", new TestElementDTO("/actor/{id}/add", "Add an actor")});
        model.addRow(new Object[] {"t6", new TestElementDTO("/actor/{id}/update", "Update an actor")});
        model.addRow(new Object[] {"t7", new TestElementDTO("/resource/{id}/delete", "Delete a resource")});

        ListRowFilter<TableModel, Object> listRowFilter = new ListRowFilter<>();
        TableRowSorter<TableModel> defaultRowSorter = new TableRowSorter<>(model);
        defaultRowSorter.setRowFilter(listRowFilter);
        JTable table = new JTable(model);
        table.setRowSorter(defaultRowSorter);

        listRowFilter.updateFilter(searchText, isRegexMode);
        defaultRowSorter.sort();
        table.selectAll();
        Assertions.assertEquals(
                expectedResult,
                String.join(
                        ",",
                        Arrays.stream(table.getSelectionModel().getSelectedIndices())
                                .mapToObj(index ->
                                        table.getModel().getValueAt(index, 0).toString())
                                .toList()));
    }
}
