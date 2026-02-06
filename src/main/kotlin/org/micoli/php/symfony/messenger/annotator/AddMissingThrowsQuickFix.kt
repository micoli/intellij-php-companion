package org.micoli.php.symfony.messenger.annotator

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.psi.elements.Method
import org.micoli.php.service.intellij.psi.PhpUtil

class AddMissingThrowsQuickFix(private val method: Method, private val missingThrows: Set<String>) :
    IntentionAction {

    override fun getText(): String = "Add missing @throws for handler exceptions"

    override fun getFamilyName(): String = "Php companion"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return method.isValid && missingThrows.isNotEmpty()
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return

        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
        val docComment = method.docComment as? PhpDocComment

        if (docComment != null) {
            addThrowsToExistingDocComment(document, docComment)
        } else {
            createNewDocCommentWithThrows(document)
        }

        PsiDocumentManager.getInstance(project).commitDocument(document)
    }

    private fun addThrowsToExistingDocComment(document: Document, docComment: PhpDocComment) {
        val docText = docComment.text
        val closingIndex = docText.lastIndexOf("*/")
        if (closingIndex < 0) return

        val insertOffset = docComment.textRange.startOffset + closingIndex

        // Find the start of the */ line in the document
        val fullText = document.text
        val lineStart = fullText.lastIndexOf('\n', insertOffset - 1)

        // Extract indentation used before */ (e.g. "     " for "     */")
        val linePrefix = if (lineStart >= 0) fullText.substring(lineStart + 1, insertOffset) else ""

        val throwsLines =
            missingThrows.sorted().joinToString("\n") {
                "$linePrefix* @throws ${PhpUtil.normalizeRootFQN(it)}"
            }

        // Insert at the beginning of the */ line, so the existing */ line keeps its indentation
        val insertAt = if (lineStart >= 0) lineStart + 1 else insertOffset
        document.insertString(insertAt, throwsLines + "\n")
    }

    private fun createNewDocCommentWithThrows(document: Document) {
        val fullText = document.text
        val methodOffset = method.textRange.startOffset

        // Find the start of the method's line and extract its indentation
        val lineStart = fullText.lastIndexOf('\n', methodOffset - 1)
        val indent =
            if (lineStart >= 0) {
                fullText.substring(lineStart + 1, methodOffset).takeWhile {
                    it == ' ' || it == '\t'
                }
            } else {
                ""
            }

        val newDocComment = buildString {
            append(indent)
            append("/**\n")
            for (fqcn in missingThrows.sorted()) {
                append(indent)
                append(" * @throws ")
                append(PhpUtil.normalizeRootFQN(fqcn))
                append("\n")
            }
            append(indent)
            append(" */\n")
        }

        // Insert at the beginning of the method's line
        val insertAt = if (lineStart >= 0) lineStart + 1 else methodOffset
        document.insertString(insertAt, newDocComment)
    }

    override fun startInWriteAction(): Boolean = true

    private fun getIndentation(text: String, offset: Int): String {
        var lineStart = offset
        while (lineStart > 0 && text[lineStart - 1] != '\n') {
            lineStart--
        }
        val linePrefix = text.substring(lineStart, offset)
        return linePrefix.takeWhile { it == ' ' || it == '\t' }
    }
}
