package org.micoli.php.openAPI;

import com.intellij.psi.PsiElement;

public record OpenAPIPathElementDTO(
        String uri, String method, String description, String operationId, PsiElement element) {}
