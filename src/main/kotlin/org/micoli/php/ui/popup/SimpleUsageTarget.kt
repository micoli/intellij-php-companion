package org.micoli.php.ui.popup

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.usages.UsageTarget
import javax.swing.Icon

internal class SimpleUsageTarget(val element: PsiElement?, name: String?) : UsageTarget {
    private val name: String = name ?: "Usage Target"

    override fun getName(): String {
        if (element is PsiNamedElement) {
            val elementName = element.name
            return elementName ?: name
        }
        return name
    }

    override fun findUsages() {}

    override fun isValid(): Boolean {
        return element == null || element.isValid
    }

    override fun canNavigate(): Boolean {
        return element != null && element.isValid
    }

    override fun navigate(requestFocus: Boolean) {}

    override fun canNavigateToSource(): Boolean {
        return canNavigate()
    }

    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                return getName()
            }

            override fun getLocationString(): String? {
                return null
            }

            override fun getIcon(unused: Boolean): Icon? {
                return null
            }
        }
    }

    override fun isReadOnly(): Boolean {
        return false
    }
}
