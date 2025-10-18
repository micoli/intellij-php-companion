package org.micoli.php.ui.table

import java.util.Vector
import javax.swing.table.DefaultTableModel

open class ObjectTableModel<T>(columnNames: Array<String>) : DefaultTableModel(columnNames, 0) {
    protected var objects: Vector<T> = Vector()

    val lock = Any()

    override fun isCellEditable(row: Int, column: Int): Boolean {
        return false
    }

    fun getObjectAt(row: Int): T? {
        return try {
            objects[row]
        } catch (_: NullPointerException) {
            null
        }
    }

    fun arrayToVector(anArray: Array<Any?>?): Vector<Any?>? {
        if (anArray == null) {
            return null
        }
        val v = Vector<Any?>(anArray.size)
        for (o in anArray) {
            v.addElement(o)
        }
        return v
    }

    fun setRows(elements: List<T>, mapper: (item: T) -> Array<Any?>) {
        val itemsVector = Vector<T>()
        val columnsVector = Vector<Vector<Any?>?>()
        for (item in elements) {
            itemsVector.add(item)
            columnsVector.add(arrayToVector(mapper(item)))
        }
        objects = itemsVector
        dataVector = columnsVector
    }

    fun addRow(associatedObject: T, rowData: Array<Any?>) {
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun removeRow(row: Int) {
        throw UnsupportedOperationException("Not supported yet.")
    }
}
