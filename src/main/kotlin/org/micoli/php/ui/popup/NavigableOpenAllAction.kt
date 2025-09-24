package org.micoli.php.ui.popup

import com.intellij.pom.Navigatable
import java.util.function.Consumer
import kotlin.Boolean
import kotlin.text.trimIndent

class NavigableOpenAllAction(private val navigables: MutableList<Navigatable?>) :
    NavigableListPopupItem {
    override fun navigate(requestFocus: Boolean) {
        this.navigables.forEach(
            Consumer { navigable: Navigatable? -> navigable!!.navigate(requestFocus) })
    }

    override fun canNavigate(): Boolean {
        return true
    }

    override fun canNavigateToSource(): Boolean {
        return false
    }

    override fun getText(): String {
        return String.format(
            """
                <html>
                    <div style="padding:5px">
                        <i style="color: orange;font-weight:bold">%s</i>
                    </div>
                </html>,
                
                """
                .trimIndent(),
            "Open results in Editor",
        )
    }
}
