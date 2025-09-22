package org.micoli.php.ui.popup

import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.usageView.UsageInfo
import com.intellij.usages.*
import kotlin.Boolean
import kotlin.arrayOf
import kotlin.text.trimIndent

class NavigableOpenSearchAction(
  private val project: Project,
  private val navigables: MutableList<Navigatable?>?,
  private val findWindowTitle: String?,
  private val messageClassName: String?,
) : NavigableListPopupItem {
    override fun navigate(requestFocus: Boolean) {
        if (this.navigables == null) {
            return
        }
        val usages: MutableList<Usage?> = ArrayList()
        var element: PsiElement? = null
        for (navigatable in navigables) {
            element = extractPsiElement(navigatable)
            if (element != null && element.isValid) {
                usages.add(UsageInfo2UsageAdapter(UsageInfo(element)))
            }
        }
        if (usages.isEmpty()) {
            return
        }

        val target: UsageTarget = SimpleUsageTarget(element, messageClassName)
        val targets = arrayOf<UsageTarget?>(target)
        val presentation = UsageViewPresentation()
        presentation.tabText = findWindowTitle
        presentation.toolwindowTitle = findWindowTitle
        presentation.isShowCancelButton = true

        UsageViewManager.getInstance(project).showUsages(targets, usages.toTypedArray(), presentation)
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
                </html>
                
                """
            .trimIndent(),
          "Open results in Find window",
        )
    }

    companion object {
        private fun extractPsiElement(navigatable: Navigatable?): PsiElement? {
            if (navigatable is PsiElement) {
                return navigatable
            }

            if (navigatable is SmartPsiElementPointer<*>) {
                return navigatable.getElement()
            }

            return null
        }
    }
}
