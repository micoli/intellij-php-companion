package org.micoli.php.classStyles

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import java.awt.Font
import org.micoli.php.classStyles.configuration.ClassStylesConfiguration
import org.micoli.php.classStyles.configuration.FontStyle
import org.micoli.php.service.intellij.psi.PhpUtil

@Service(Service.Level.PROJECT)
class ClassStylesService {
    var enabled: Boolean = false
    var configuration: ClassStylesConfiguration = ClassStylesConfiguration()
    val rules: MutableList<ClassStyleRule> = emptyList<ClassStyleRule>().toMutableList()

    class ClassStyleRule(val fqcns: Array<String>, val style: TextAttributes) {
        fun isElligible(project: Project, phpType: PhpType): Boolean {
            return PhpUtil.implementsInterfaces(project, phpType, fqcns)
        }
    }

    fun loadConfiguration(stylesConfiguration: ClassStylesConfiguration?) {
        if (stylesConfiguration == null) {
            enabled = false
            return
        }
        enabled = true
        configuration = stylesConfiguration
        synchronized(this) {
            rules.clear()
            rules.addAll(
                configuration.rules.map {
                    ClassStyleRule(
                        it.fqcns,
                        TextAttributes(
                            it.style.foregroundColor?.color,
                            it.style.backgroundColor?.color,
                            it.style.effectColor?.color,
                            it.style.effect,
                            it.style.fontStyles.sumOf { style ->
                                when (style) {
                                    FontStyle.BOLD -> Font.BOLD
                                    FontStyle.ITALIC -> Font.ITALIC
                                    else -> 0
                                }
                            }))
                })
        }
    }

    fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!enabled || rules.isEmpty()) return
        synchronized(this) {
            val p =
                when (element) {
                    is PhpClass ->
                        element.nameIdentifier?.let { Pair(element.declaredType, it.textRange) }

                    is NewExpression ->
                        element.classReference?.let { Pair(it.resolveLocalType(), it.textRange) }

                    is MethodReference ->
                        element.classReference?.let { Pair(it.declaredType, it.textRange) }

                    else -> null
                } ?: return

            for (rule in rules) {
                if (rule.isElligible(element.project, p.first)) {
                    holder
                        .newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(p.second)
                        .enforcedTextAttributes(rule.style)
                        .create()
                    return
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ClassStylesService {
            return project.getService(ClassStylesService::class.java)
        }
    }
}
