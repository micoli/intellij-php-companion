package org.micoli.php.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

public class PsiElementUtil {
    public static PsiElement findFirstLeafElement(PsiElement element) {
        PsiElement children = element.getFirstChild();
        if (children == null) {
            return element;
        }
        return findFirstLeafElement(children);
    }

    public static int getLineNumber(PsiElement element) {
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(element.getContainingFile());

        if (document != null) {
            return document.getLineNumber(element.getTextRange().getStartOffset()) + 1;
        }

        return -1;
    }

    public static @NotNull String getHumanReadableElementLink(PsiElement element) {
        return ApplicationManager.getApplication().runReadAction((Computable<String>) () -> {
            String base = PathUtil.getPathWithParent(element.getContainingFile(), 2);

            if (element instanceof PsiNamedElement) {
                return base + "#" + getLineNumber(element) + ": " + ((PsiNamedElement) element).getName();
            }

            return base + "#" + getLineNumber(element) + ": " + element.getText().replaceAll("\\s( *)", " ");
        });

    }

}
