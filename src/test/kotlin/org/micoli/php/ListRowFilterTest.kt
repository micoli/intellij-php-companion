package org.micoli.php

import java.io.IOException
import java.util.Arrays
import java.util.function.IntFunction
import javax.swing.JTable
import javax.swing.table.TableModel
import javax.swing.table.TableRowSorter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.micoli.php.symfony.list.SearchableRecord
import org.micoli.php.ui.table.ListRowFilter
import org.micoli.php.ui.table.ObjectTableModel

@RunWith(Parameterized::class)
class ListRowFilterTest(
    private val searchText: String,
    private val isRegexMode: Boolean,
    private val expectedResult: String,
) {
    @JvmRecord
    data class TestElementDTO(val id: String, val uri: String, val name: String) :
        SearchableRecord {
        override fun getSearchString(): ImmutableList<String> = persistentListOf(uri, name)
    }

    @Test
    fun testItFiltersTableRows() {
        val columnNames = arrayOf("Name", "Element")
        val model = ObjectTableModel<TestElementDTO>(columnNames)

        model.setRows(
            listOf(
                TestElementDTO("t1", "/test/toto", "A good description"),
                TestElementDTO("t2", "/test/toto", "A bad description"),
                TestElementDTO("t3", "/test/tata/toto", "Without desc"),
                TestElementDTO("t4", "/test/tata/tutu", "Without desc"),
                TestElementDTO("t5", "/actor/{id}/add", "Add an actor"),
                TestElementDTO("t6", "/actor/{id}/update", "Update an actor"),
                TestElementDTO("t7", "/resource/{id}/delete", "Delete a resource"),
            ),
            { item -> arrayOf(item.id) })

        val listRowFilter = ListRowFilter<TableModel, Any?>()
        val defaultRowSorter = TableRowSorter<TableModel>(model)
        defaultRowSorter.setRowFilter(listRowFilter)
        val table = JTable(model)
        table.setRowSorter(defaultRowSorter)

        listRowFilter.updateFilter(searchText, isRegexMode)
        defaultRowSorter.sort()
        table.selectAll()
        Assertions.assertEquals(
            expectedResult,
            Arrays.stream(table.getSelectionModel().getSelectedIndices())
                .mapToObj(IntFunction { index: Int -> table.model.getValueAt(index, 0).toString() })
                .toList()
                .joinToString(","),
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "Filter with \"{0}\" and isRegex is {1}")
        @Throws(IOException::class)
        fun parameters(): Collection<Array<Any?>> =
            listOf(
                arrayOf("", false, "t1,t2,t3,t4,t5,t6,t7"),
                arrayOf("", true, "t1,t2,t3,t4,t5,t6,t7"),
                arrayOf("good", false, "t1"),
                arrayOf("description", false, "t1,t2"),
                arrayOf("test", false, "t1,t2,t3,t4"),
                arrayOf("nonexistent", false, ""),
                arrayOf("/test/.*", false, ""),
                arrayOf("/test/.*toto", true, "t1,t2,t3"),
                arrayOf("/test/.*", true, "t1,t2,t3,t4"),
                arrayOf(".*\\{id\\}.*", true, "t1,t2,t3"),
                arrayOf("^/actor.*", true, "t1,t2"),
                arrayOf("resource.*delete", true, "t1"),
                arrayOf("acToR update", false, "t1"),
                arrayOf("ACTOR", false, "t1,t2"),
                arrayOf("Update", false, "t1"),
                arrayOf("/{id}/", false, "t1,t2,t3"),
                arrayOf("toto", false, "t1,t2,t3"),
                arrayOf(".*toto$", true, "t1,t2,t3"),
            )
    }
}
