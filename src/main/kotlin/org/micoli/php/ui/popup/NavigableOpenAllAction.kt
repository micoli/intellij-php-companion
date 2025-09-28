package org.micoli.php.ui.popup

import com.intellij.pom.Navigatable
import kotlin.Boolean
import kotlin.text.trimIndent
import kotlinx.collections.immutable.ImmutableList

class NavigableOpenAllAction(private val navigables: ImmutableList<Navigatable?>) :
    NavigableListPopupItem {
    override fun navigate(requestFocus: Boolean) {
        this.navigables.filter { it != null }.forEach { it!!.navigate(requestFocus) }
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
