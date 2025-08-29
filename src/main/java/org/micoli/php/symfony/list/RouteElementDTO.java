package org.micoli.php.symfony.list;

import com.intellij.psi.PsiElement;

public record RouteElementDTO(String uri, String name, String methods, String fqcn, PsiElement element)
        implements SearchableRecord {

    @Override
    public String getSearchString() {
        return uri + " " + name + " " + methods + " " + fqcn;
    }
}
