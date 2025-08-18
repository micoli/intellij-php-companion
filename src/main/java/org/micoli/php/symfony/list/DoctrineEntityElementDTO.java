package org.micoli.php.symfony.list;

import com.intellij.psi.PsiElement;

public record DoctrineEntityElementDTO(String className, String name, String schema, String fqcn, PsiElement element) {}
