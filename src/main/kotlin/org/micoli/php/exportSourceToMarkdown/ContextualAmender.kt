package org.micoli.php.exportSourceToMarkdown

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.PhpUseList
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration
import org.micoli.php.service.intellij.psi.PhpUtil.getVirtualFileFromFQN
import org.micoli.php.service.intellij.psi.PhpUtil.normalizeRootFQN

class ContextualAmender(
    project: Project,
    private val configuration: ExportSourceToMarkdownConfiguration
) {
    private val phpIndex: PhpIndex = PhpIndex.getInstance(project)
    private val psiManager: PsiManager = PsiManager.getInstance(project)

    fun amendListWithContextualFiles(
        paramProcessedFiles: MutableList<VirtualFile>
    ): MutableList<VirtualFile> {
        var processedFiles = paramProcessedFiles
        var count: Int
        do {
            count = processedFiles.size
            processedFiles = innerAmendListWithContextualFiles(processedFiles)
        } while (count != processedFiles.size)
        return processedFiles
    }

    fun innerAmendListWithContextualFiles(
        processedFiles: MutableList<VirtualFile>
    ): MutableList<VirtualFile> {
        val filesInContext: MutableList<VirtualFile> = ArrayList(processedFiles)
        if (configuration.contextualNamespaces == null) {
            return filesInContext
        }
        val additionalFiles: MutableList<VirtualFile> = ArrayList()

        for (virtualFile in processedFiles) {
            val psiFile = psiManager.findFile(virtualFile)
            if (psiFile !is PhpFile) {
                continue
            }
            for (fqnImport in getImports(psiFile)) {
                if (!matchContextualNamespace(fqnImport)) {
                    continue
                }
                val virtualFileFromFQN =
                    getVirtualFileFromFQN(phpIndex, normalizeRootFQN(fqnImport)) ?: continue
                if (additionalFiles.contains(virtualFileFromFQN) ||
                    processedFiles.contains(virtualFileFromFQN)) {
                    continue
                }
                additionalFiles.add(virtualFileFromFQN)
            }
        }
        additionalFiles.addAll(processedFiles)

        return additionalFiles
    }

    private fun matchContextualNamespace(fqnImport: String): Boolean {
        for (contextualNamespace in configuration.contextualNamespaces!!) {
            if (fqnImport.startsWith(normalizeRootFQN(contextualNamespace!!))) {
                return true
            }
        }
        return false
    }

    private fun getImports(phpFile: PhpFile?): MutableList<String> {
        val imports: MutableList<String> = ArrayList()

        PsiTreeUtil.processElements(phpFile) { element: PsiElement? ->
            if (element is PhpUseList) {
                for (use in element.declarations) {
                    imports.add(use.fqn)
                }
            }
            true
        }
        return imports
    }
}
