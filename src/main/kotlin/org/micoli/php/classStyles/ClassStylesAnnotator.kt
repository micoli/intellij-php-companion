package org.micoli.php.classStyles

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class ClassStylesAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        ClassStylesService.getInstance(element.project).annotate(element, holder)
    }
}
