package org.micoli.php.service.intellij.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.Variable
import org.micoli.php.service.intellij.PhpIndexUtil

object PhpUtil {
    @JvmStatic
    fun normalizeRootFQN(fqn: String): String {
        if (fqn.startsWith("\\")) {
            return fqn
        }
        return "\\" + fqn
    }

    @JvmStatic
    fun normalizeNonRootFQN(fqn: String): String {
        if (fqn.startsWith("\\")) {
            return fqn.substring(1)
        }
        return fqn
    }

    @JvmStatic
    fun getPhpClassByFQN(project: Project, fqn: String?): PhpClass? {
        val classes = PhpIndex.getInstance(project).getClassesByFQN(fqn)

        return if (classes.isEmpty()) null else classes.iterator().next()
    }

    @JvmStatic
    fun getVirtualFileFromFQN(phpIndex: PhpIndex, fqn: String?): VirtualFile? {
        val classes = phpIndex.getClassesByFQN(fqn)

        if (classes.isEmpty()) {
            return null
        }
        val phpClass = classes.iterator().next()
        val containingFile = phpClass.containingFile
        if (containingFile != null) {
            return containingFile.virtualFile
        }
        return null
    }

    @JvmStatic
    fun implementsInterfaces(phpClass: PhpClass, interfaceFQNs: Array<String>): Boolean {
        val phpIndex = PhpIndex.getInstance(phpClass.project)
        for (interfaceFQN in interfaceFQNs) {
            if (PhpIndexUtil.getAllSubclasses(phpIndex, normalizeRootFQN(interfaceFQN))
                .stream()
                .map { it.fqn }
                .toList()
                .contains(phpClass.fqn)) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun hasAttribute(phpClass: PhpClass, attributeFQN: String): Boolean {
        // TODO: Implement PHP 8 attribute parsing
        // For now, check for attribute in comments as fallback
        val docComment1 = phpClass.docComment
        val docComment = if (docComment1 != null) docComment1.text else ""
        return docComment.contains(attributeFQN)
    }

    @JvmStatic
    fun getShortClassName(fqn: String?): String {
        if (fqn == null) return ""
        val parts: Array<String?> =
            fqn.split("\\\\".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return parts[parts.size - 1] ?: ""
    }

    @JvmStatic
    fun findClassByFQN(project: Project, fqn: String?): PhpClass? {
        val phpIndex = PhpIndex.getInstance(project)
        val classes = phpIndex.getClassesByFQN(fqn)

        if (!classes.isEmpty()) {
            return classes.iterator().next()
        }

        return null
    }

    @JvmStatic
    fun getFirstParameterType(parameters: Array<PsiElement?>): String? {
        if (parameters.isEmpty()) {
            return null
        }
        if (parameters[0] is NewExpression) {
            return (parameters[0] as NewExpression).classReference?.fqn
        }

        if (parameters[0] is Variable) {
            for (type in (parameters[0] as Variable).type.types) {
                if (type.startsWith("\\")) {
                    return type
                }
            }
        }

        return null
    }

    @JvmStatic
    fun getPhpClassChildByFQN(phpIndex: PhpIndex, className: String): Array<String> {
        val phpClasses: MutableCollection<PhpClass> = ArrayList()

        phpIndex.processAllSubclasses(className) {
            phpClasses.add(it)
            true
        }
        return phpClasses.stream().filter { it != null }.map { it.fqn }.toList().toTypedArray()
    }
}
