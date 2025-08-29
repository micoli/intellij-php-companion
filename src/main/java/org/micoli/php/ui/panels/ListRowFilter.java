package org.micoli.php.ui.panels;

import com.intellij.openapi.diagnostic.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import org.micoli.php.symfony.list.SearchableRecord;

public class ListRowFilter<M, I> extends RowFilter<M, I> {
    protected static final Logger LOGGER = Logger.getInstance(ListRowFilter.class);
    private String searchText = "";
    private List<Pattern> searchPatterns = null;
    private List<String> searchParts = null;
    private boolean isRegexMode = true;

    public void updateFilter(String searchText, boolean isRegexMode) {
        this.searchText = searchText;
        this.isRegexMode = isRegexMode;
        searchPatterns = isRegexMode
                ? Arrays.stream(searchText.split(" "))
                        .map(String::trim)
                        .map(expr -> Pattern.compile("(?i)" + expr))
                        .toList()
                : null;
        searchParts = !isRegexMode
                ? Arrays.stream(searchText.split(" ")).map(String::trim).toList()
                : null;
    }

    @Override
    public boolean include(Entry<? extends M, ? extends I> entry) {
        if (searchText.isEmpty()) {
            return true;
        }
        if (entry.getValue(entry.getValueCount() - 1) instanceof SearchableRecord searchableRecord) {
            if (isRegexMode) {
                return searchPatterns.stream().allMatch(pattern -> pattern.matcher(searchableRecord.getSearchString())
                        .find());
            }
            return searchParts.stream()
                    .allMatch(part ->
                            searchableRecord.getSearchString().toLowerCase().contains(part.toLowerCase()));
        }
        return false;
    }
}
