package org.micoli.php.ui.table

import java.text.NumberFormat
import java.util.Locale
import javax.swing.table.DefaultTableCellRenderer

class DoubleCellRenderer(locale: Locale = Locale.getDefault(), private val decimals: Int = 2) :
    DefaultTableCellRenderer() {
    private val formatter =
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = decimals
            maximumFractionDigits = decimals
        }

    init {
        horizontalAlignment = RIGHT
    }

    override fun setValue(value: Any?) {
        text =
            when (value) {
                is Number -> formatter.format(value.toDouble())
                else -> value?.toString() ?: ""
            }
    }
}
