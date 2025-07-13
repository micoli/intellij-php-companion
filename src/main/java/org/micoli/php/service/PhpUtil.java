package org.micoli.php.service;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

public class PhpUtil {
    public static String normalizeRootFQN(String fqn) {
        if (fqn.startsWith("\\")) {
            return fqn;
        }
        return "\\" + fqn;
    }

    public static String normalizeNonRootFQN(String fqn) {
        if (fqn.startsWith("\\")) {
            return fqn.substring(1);
        }
        return fqn;
    }

    public static PhpClass getPhpClassByFQN(Project project, String fqn) {
        Collection<PhpClass> classes = PhpIndex.getInstance(project).getClassesByFQN(fqn);

        return classes.isEmpty() ? null : classes.iterator().next();
    }

    public static VirtualFile getVirtualFileFromFQN(PhpIndex phpIndex, String fqn) {
        Collection<PhpClass> classes = phpIndex.getClassesByFQN(fqn);

        if (classes.isEmpty()) {
            return null;
        }
        PhpClass phpClass = classes.iterator().next();
        PsiFile containingFile = phpClass.getContainingFile();
        if (containingFile != null) {
            return containingFile.getVirtualFile();
        }
        return null;

    }

    public static boolean implementsInterfaces(PhpClass phpClass, String[] interfaceFQNs) {
        PhpIndex phpIndex = PhpIndex.getInstance(phpClass.getProject());
        for (String interfaceFQN : interfaceFQNs) {
            if (PhpIndexUtil.getAllSubclasses(phpIndex, normalizeRootFQN(interfaceFQN)).stream().map(PhpNamedElement::getFQN).toList().contains(phpClass.getFQN())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAttribute(PhpClass phpClass, String attributeFQN) {
        // TODO: Implement PHP 8 attribute parsing
        // For now, check for attribute in comments as fallback
        String docComment = phpClass.getDocComment() != null ? phpClass.getDocComment().getText() : "";
        return docComment.contains(attributeFQN);
    }

    public static String getShortClassName(String fqn) {
        if (fqn == null)
            return "";
        String[] parts = fqn.split("\\\\");
        return parts[parts.length - 1];
    }

    public static @Nullable PhpClass findClassByFQN(Project project, String fqn) {
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<PhpClass> classes = phpIndex.getClassesByFQN(fqn);

        if (!classes.isEmpty()) {
            return classes.iterator().next();
        }

        return null;
    }

    public static @Nullable String getFirstParameterType(PsiElement[] parameters) {
        if (parameters.length == 0) {
            return null;
        }
        if (parameters[0] instanceof NewExpression newExpr) {
            ClassReference classReference = newExpr.getClassReference();
            return classReference == null ? null : classReference.getFQN();
        }

        if (parameters[0] instanceof Variable variable) {
            for (String type : variable.getType().getTypes()) {
                if (type.startsWith("\\")) {
                    return type;
                }
            }
        }

        return null;
    }
}
