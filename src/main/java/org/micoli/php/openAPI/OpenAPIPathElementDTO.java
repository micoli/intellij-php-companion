package org.micoli.php.openAPI;

import com.intellij.psi.PsiElement;
import java.util.List;
import org.micoli.php.symfony.list.SearchableRecord;

public record OpenAPIPathElementDTO(
        String rootPath, String uri, String method, String description, String operationId, PsiElement element)
        implements SearchableRecord {

    @Override
    public List<String> getSearchString() {
        return List.of(uri, method, description, operationId, rootPath);
    }
}
