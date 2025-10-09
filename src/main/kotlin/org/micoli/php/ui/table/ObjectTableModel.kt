package org.micoli.php.ui.table

import java.util.Vector
import javax.swing.table.DefaultTableModel

open class ObjectTableModel<T>(columnNames: Array<String>) : DefaultTableModel(columnNames, 0) {
    protected val objects: Vector<T> = Vector()

    override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
    }

    fun addRow(associatedObject: T, rowData: Array<Any?>) {
        objects.insertElementAt(associatedObject, rowCount)
        super.addRow(rowData + (associatedObject as Any))
    }

    fun getObjectAt(row: Int): T? {
        return try {
            objects[row]
        } catch (_: NullPointerException) {
            null
        }
    }

    override fun removeRow(row: Int) {
        objects.removeAt(row)
        super.removeRow(row)
    }

    fun clearRows() {
        while (rowCount > 0) {
            removeRow(0)
        }
    }
}
