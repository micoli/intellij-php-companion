package org.micoli.php.service.intellij.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.ui.awt.RelativePoint
import java.awt.event.MouseEvent

object PsiElementsPopup {
    @JvmStatic
    fun showLinksToElementsPopup(mouseEvent: MouseEvent, elements: MutableList<PsiElement?>) {
        showPopup(mouseEvent, elements)
    }

    private fun showPopup(mouseEvent: MouseEvent, elements: MutableList<PsiElement?>) {
        JBPopupFactory.getInstance()
            .createListPopup(
                object : BaseListPopupStep<PsiElement>("Navigate to Element", elements) {
                    override fun onChosen(
                        selectedValue: PsiElement?,
                        finalChoice: Boolean
                    ): PopupStep<*>? {
                        if (finalChoice && selectedValue is Navigatable) {
                            ApplicationManager.getApplication().invokeLater {
                                selectedValue.navigate(true)
                            }
                        }
                        return FINAL_CHOICE
                    }

                    override fun getTextFor(element: PsiElement?): String {
                        return if (element == null) ""
                        else PsiElementUtil.getHumanReadableElementLink(element)
                    }

                    override fun isSpeedSearchEnabled(): Boolean {
                        return true
                    }
                })
            .show(RelativePoint(mouseEvent))
    }
}
