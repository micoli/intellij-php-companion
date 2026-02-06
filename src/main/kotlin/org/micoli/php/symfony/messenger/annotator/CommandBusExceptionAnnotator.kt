package org.micoli.php.symfony.messenger.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class CommandBusExceptionAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        CommandBusExceptionService.getInstance(element.project).annotate(element, holder)
    }
}
