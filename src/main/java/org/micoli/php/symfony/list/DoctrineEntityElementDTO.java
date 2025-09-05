package org.micoli.php.symfony.list;

import com.intellij.psi.PsiElement;
import java.util.List;

public record DoctrineEntityElementDTO(String className, String name, String schema, String fqcn, PsiElement element)
        implements SearchableRecord {

    @Override
    public List<String> getSearchString() {
        return List.of(className, name, schema, fqcn);
    }
}
