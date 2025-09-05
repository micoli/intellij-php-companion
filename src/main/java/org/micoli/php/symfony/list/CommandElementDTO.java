package org.micoli.php.symfony.list;

import com.intellij.psi.PsiElement;
import java.util.List;

public record CommandElementDTO(String command, String description, String className, PsiElement element)
        implements SearchableRecord {

    @Override
    public List<String> getSearchString() {
        return List.of(command, description, className);
    }
}
