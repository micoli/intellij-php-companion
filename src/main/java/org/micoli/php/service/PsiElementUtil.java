package org.micoli.php.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.usageView.UsageInfo;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.ui.popup.FileExtract;

public class PsiElementUtil {
    public static PsiElement findFirstLeafElement(PsiElement element) {
        PsiElement children = element.getFirstChild();
        if (children == null) {
            return element;
        }
        return findFirstLeafElement(children);
    }

    public static FileExtract getFileExtract(PsiElement element, int lineCount) {
        return getFileExtract(element.getProject(), element.getContainingFile(), element.getTextRange(), lineCount);
    }

    public static FileExtract getFileExtract(UsageInfo usageInfo, int lineCount) {
        return getFileExtract(usageInfo.getProject(), usageInfo.getFile(), usageInfo.getRangeInElement(), lineCount);
    }

    public static FileExtract getFileExtract(Project project, PsiFile file, TextRange range, int lineCount) {
        Document document = PsiDocumentManager.getInstance(project).getDocument(file);

        if (document != null) {
            int lineNumber = document.getLineNumber(range.getStartOffset());

            return new FileExtract(
                    lineNumber + 1,
                    document.getText(TextRange.create(
                            document.getLineStartOffset(lineNumber),
                            document.getLineEndOffset(lineNumber + lineCount))));
        }

        return new FileExtract(-1, "");
    }

    public static @NotNull String getHumanReadableElementLink(PsiElement element) {

        return ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
            String base = PathUtil.getPathWithParent(element.getContainingFile(), 2);
            String format = "%s#%s: %s";

            if (element instanceof PsiNamedElement) {
                return String.format(
                        format, base, getFileExtract(element, 1).lineNumber(), ((PsiNamedElement) element).getName());
            }

            return String.format(
                    format,
                    base,
                    getFileExtract(element, 1).lineNumber(),
                    element.getText().replaceAll("\\s( *)", " "));
        });
    }
}
