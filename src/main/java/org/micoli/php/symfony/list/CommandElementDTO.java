package org.micoli.php.symfony.list;

import com.intellij.psi.PsiElement;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public record CommandElementDTO(
        @NotNull String command, @NotNull String description, @NotNull String className, @NotNull PsiElement element)
        implements SearchableRecord {

    @Override
    public List<@NotNull String> getSearchString() {
        return List.of(command, description, className);
    }
}
