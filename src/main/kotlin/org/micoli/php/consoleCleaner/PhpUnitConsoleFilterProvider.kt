package org.micoli.php.consoleCleaner

import com.intellij.execution.filters.ConsoleDependentInputFilterProvider
import com.intellij.execution.filters.InputFilter
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.containers.stream
import java.util.regex.Pattern
import org.micoli.php.configuration.models.Configuration
import org.micoli.php.consoleCleaner.configuration.ConsoleCleanerConfiguration
import org.micoli.php.events.ConfigurationEvents

class PhpUnitConsoleFilterProvider : ConsoleDependentInputFilterProvider() {
    @JvmRecord private data class PatternMatcher(val isFullLine: Boolean, val pattern: Pattern)

    private var patterns: MutableList<PatternMatcher> = ArrayList<PatternMatcher>()

    override fun getDefaultFilters(
        consoleView: ConsoleView,
        project: Project,
        globalSearchScope: GlobalSearchScope
    ): MutableList<InputFilter?> {
        project.messageBus
            .connect()
            .subscribe(
                ConfigurationEvents.CONFIGURATION_UPDATED,
                object : ConfigurationEvents {
                    override fun configurationLoaded(loadedConfiguration: Configuration?) {
                        loadConfiguration(loadedConfiguration!!.consoleCleaner)
                    }
                })
        return mutableListOf(cleanup())
    }

    private fun loadConfiguration(configuration: ConsoleCleanerConfiguration?) {
        if (configuration == null) {
            patterns.clear()
            return
        }
        patterns =
            configuration.patterns
                .stream()
                .map { p: String ->
                    PatternMatcher(p.startsWith("^") && p.endsWith("$"), Pattern.compile(p))
                }
                .toList()
    }

    fun cleanup(): InputFilter {
        return object : InputFilter {
            override fun applyFilter(
                consoleText: String,
                contentType: ConsoleViewContentType
            ): MutableList<Pair<String, ConsoleViewContentType>> {
                var consoleText = consoleText
                if (patterns.isEmpty() || consoleText.isEmpty()) {
                    return mutableListOf(
                        Pair<String, ConsoleViewContentType>(consoleText, contentType))
                }

                for (patternLineMatcher in patterns) {
                    if (!patternLineMatcher.isFullLine) {
                        continue
                    }
                    if (patternLineMatcher.pattern.matcher(consoleText).find()) {
                        return mutableListOf(
                            Pair<String, ConsoleViewContentType>(null, contentType))
                    }
                }
                for (patternMatcher in patterns) {
                    if (patternMatcher.isFullLine) {
                        continue
                    }
                    consoleText =
                        consoleText.replace(patternMatcher.pattern.pattern().toRegex(), "")
                }
                return mutableListOf(Pair<String, ConsoleViewContentType>(consoleText, contentType))
            }
        }
    }
}
