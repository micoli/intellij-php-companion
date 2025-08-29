package org.micoli.php.symfony.list;

import com.intellij.psi.PsiElement;

public record CommandElementDTO(String command, String description, String className, PsiElement element)
        implements SearchableRecord {

    @Override
    public String getSearchString() {
        return command + " " + description + " " + className;
    }
}
