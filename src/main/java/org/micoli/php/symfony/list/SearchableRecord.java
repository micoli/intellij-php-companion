package org.micoli.php.symfony.list;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface SearchableRecord {
    List<@NotNull String> getSearchString();
}
