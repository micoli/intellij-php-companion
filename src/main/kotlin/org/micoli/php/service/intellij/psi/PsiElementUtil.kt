package org.micoli.php.service.intellij.psi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.usageView.UsageInfo
import org.micoli.php.service.filesystem.PathUtil
import org.micoli.php.ui.popup.FileExtract

object PsiElementUtil {
    @JvmStatic
    fun findFirstLeafElement(element: PsiElement): PsiElement {
        val children = element.firstChild ?: return element
        return findFirstLeafElement(children)
    }

    @JvmStatic
    fun getFileExtract(element: PsiElement, lineCount: Int): FileExtract {
        return getFileExtract(element.project, element.containingFile, element.textRange, lineCount)
    }

    @JvmStatic
    fun getFileExtract(usageInfo: UsageInfo, lineCount: Int): FileExtract {
        return getFileExtract(
            usageInfo.project, usageInfo.file!!, usageInfo.rangeInElement!!, lineCount)
    }

    @JvmStatic
    fun getFileExtract(
        project: Project,
        file: PsiFile,
        range: TextRange,
        lineCount: Int
    ): FileExtract {
        val document = PsiDocumentManager.getInstance(project).getDocument(file)

        if (document != null) {
            val lineNumber = document.getLineNumber(range.startOffset)

            return FileExtract(
                PathUtil.getPathWithParent(file, 2),
                lineNumber + 1,
                document.getText(
                    TextRange.create(
                        document.getLineStartOffset(lineNumber),
                        document.getLineEndOffset(lineNumber + lineCount))))
        }

        return FileExtract(null, -1, "")
    }

    @JvmStatic
    fun getHumanReadableElementLink(element: PsiElement): String {
        return ApplicationManager.getApplication()
            .runReadAction(
                Computable {
                    val base = PathUtil.getPathWithParent(element.containingFile, 2)
                    val format = "%s#%s: %s"

                    if (element is PsiNamedElement) {
                        return@Computable String.format(
                            format, base, getFileExtract(element, 1).lineNumber, element.name)
                    }
                    String.format(
                        format,
                        base,
                        getFileExtract(element, 1).lineNumber,
                        element.text.replace("\\s( *)".toRegex(), " "))
                })
    }
}
