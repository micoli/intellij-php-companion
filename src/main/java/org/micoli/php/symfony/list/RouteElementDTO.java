package org.micoli.php.symfony.list;

import com.intellij.psi.PsiElement;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public record RouteElementDTO(
        @NotNull String uri,
        @NotNull String name,
        @NotNull String methods,
        @NotNull String fqcn,
        @NotNull PsiElement element)
        implements SearchableRecord {

    @Override
    public List<@NotNull String> getSearchString() {
        return List.of(uri, name, methods, fqcn);
    }
}
