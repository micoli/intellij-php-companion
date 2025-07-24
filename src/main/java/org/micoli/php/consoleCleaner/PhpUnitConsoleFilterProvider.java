package org.micoli.php.consoleCleaner;

import com.intellij.execution.filters.ConsoleDependentInputFilterProvider;
import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.consoleCleaner.configuration.ConsoleCleanerConfiguration;
import org.micoli.php.events.ConfigurationEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PhpUnitConsoleFilterProvider extends ConsoleDependentInputFilterProvider {
    private record PatternMatcher(boolean isFullLine, Pattern pattern) {}
    private List<PatternMatcher> patterns = new ArrayList<>();

    @Override
    public @NotNull List<InputFilter> getDefaultFilters(
            @NotNull ConsoleView consoleView, @NotNull Project project, @NotNull GlobalSearchScope globalSearchScope) {
        project.getMessageBus().connect().subscribe(ConfigurationEvents.CONFIGURATION_UPDATED, (ConfigurationEvents)
                (configuration) -> loadConfiguration(configuration.consoleCleaner));
        return List.of(cleanup());
    }

    private void loadConfiguration(ConsoleCleanerConfiguration configuration) {
        if (configuration == null) {
            patterns = null;
            return;
        }
        patterns = Arrays.stream(configuration.patterns).map(p-> new PatternMatcher(p.startsWith("^" )&& p.endsWith("$"),Pattern.compile(p))).toList();
    }

    public InputFilter cleanup() {
        return new InputFilter() {
            @Override
            public @NotNull List<com.intellij.openapi.util.Pair<String, ConsoleViewContentType>> applyFilter(
                    @NotNull String consoleText, @NotNull ConsoleViewContentType contentType) {
                if (patterns == null || patterns.isEmpty() || consoleText.isEmpty()) {
                    return List.of(new Pair<>(consoleText, contentType));
                }

                for (PatternMatcher patternLineMatcher : patterns) {
                    if(!patternLineMatcher.isFullLine){
                        continue;
                    }
                    if(patternLineMatcher.pattern().matcher(consoleText).find()){
                        return List.of(new Pair<>(null, contentType));
                    }
                }
                for (PatternMatcher patternMatcher : patterns) {
                    if(patternMatcher.isFullLine){
                        continue;
                    }
                    consoleText = consoleText.replaceAll(patternMatcher.pattern().pattern(), "");
                }
                return List.of(new Pair<>(consoleText, contentType));
            }
        };
    }
}
