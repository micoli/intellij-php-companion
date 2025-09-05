package org.micoli.php.openAPI;

import com.intellij.psi.PsiElement;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.symfony.list.SearchableRecord;

public record OpenAPIPathElementDTO(
        @NotNull String rootPath,
        @NotNull String uri,
        @NotNull String method,
        @NotNull String description,
        @NotNull String operationId,
        PsiElement element)
        implements SearchableRecord {

    @Override
    public List<@NotNull String> getSearchString() {
        return List.of(uri, method, description, operationId, rootPath);
    }
}
