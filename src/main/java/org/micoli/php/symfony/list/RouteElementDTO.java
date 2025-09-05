package org.micoli.php.symfony.list;

import com.intellij.psi.PsiElement;
import java.util.List;

public record RouteElementDTO(String uri, String name, String methods, String fqcn, PsiElement element)
        implements SearchableRecord {

    @Override
    public List<String> getSearchString() {
        return List.of(uri, name, methods, fqcn);
    }
}
