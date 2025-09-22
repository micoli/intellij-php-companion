package org.micoli.php.ui.popup

import com.intellij.pom.Navigatable
import javax.swing.Icon
import kotlin.Boolean
import kotlin.text.trimIndent

class NavigableItem(@JvmField val fileExtract: FileExtract, @JvmField val navigable: Navigatable, val icon: Icon?) : NavigableListPopupItem {
    override fun navigate(requestFocus: Boolean) {
        navigable.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean {
        return true
    }

    override fun canNavigateToSource(): Boolean {
        return true
    }

    override fun getText(): String {
        return String.format(
          """
                <html>
                    <div style="padding:5px">
                        <i style="color: gray;">
                            %s:%d
                        </i>
                        <br/>
                        <code style="margin-left:5px">
                            %s
                        </code>
                    </div>
                </html>
                
                """
            .trimIndent(),
          fileExtract.file,
          fileExtract.lineNumber,
          fileExtract.text?.replace("\n", "<br/>"),
        )
    }
}
