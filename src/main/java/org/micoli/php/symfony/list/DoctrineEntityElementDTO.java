package org.micoli.php.symfony.list;

import com.intellij.psi.PsiElement;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public record DoctrineEntityElementDTO(
        @NotNull String className,
        @NotNull String name,
        @NotNull String schema,
        @NotNull String fqcn,
        @NotNull PsiElement element)
        implements SearchableRecord {

    @Override
    public List<@NotNull String> getSearchString() {
        return List.of(className, name, schema, fqcn);
    }
}
