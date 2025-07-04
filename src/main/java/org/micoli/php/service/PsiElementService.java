package org.micoli.php.service;

import com.intellij.psi.PsiElement;

public class PsiElementService {
    public static PsiElement findFirstLeafElement(PsiElement element) {
        PsiElement children = element.getFirstChild();
        if (children==null) {
            return element;
        }
        return findFirstLeafElement(children);
    }
}
