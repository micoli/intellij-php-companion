package org.micoli.php.configuration.models

import com.fasterxml.jackson.annotation.JsonAlias
import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration
import org.micoli.php.classStyles.configuration.ClassStylesConfiguration
import org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration
import org.micoli.php.consoleCleaner.configuration.ConsoleCleanerConfiguration
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration
import org.micoli.php.peerNavigation.configuration.PeerNavigationConfiguration
import org.micoli.php.runner.configuration.PhpRunnerConfiguration
import org.micoli.php.symfony.list.configuration.CommandsConfiguration
import org.micoli.php.symfony.list.configuration.DoctrineEntitiesConfiguration
import org.micoli.php.symfony.list.configuration.OpenAPIConfiguration
import org.micoli.php.symfony.list.configuration.RoutesConfiguration
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration
import org.micoli.php.symfony.profiler.configuration.SymfonyProfilerConfiguration
import org.micoli.php.tasks.configuration.TasksConfiguration

class Configuration {
    @JvmField var peerNavigation: PeerNavigationConfiguration? = null

    @JvmField var symfonyMessenger: SymfonyMessengerConfiguration? = null

    @JvmField var attributeNavigation: AttributeNavigationConfiguration? = null

    @JvmField var exportSourceToMarkdown: ExportSourceToMarkdownConfiguration? = null

    @JsonAlias("routesConfiguration") @JvmField var routes: RoutesConfiguration? = null

    @JsonAlias("commandsConfiguration") @JvmField var commands: CommandsConfiguration? = null

    @JsonAlias("doctrineEntitiesConfiguration")
    @JvmField
    var doctrineEntities: DoctrineEntitiesConfiguration? = null

    @JvmField var consoleCleaner: ConsoleCleanerConfiguration? = null

    @JsonAlias("openAPIConfiguration") @JvmField var openAPI: OpenAPIConfiguration? = null

    @JsonAlias("tasksConfiguration") @JvmField var tasks: TasksConfiguration? = null

    @JvmField var codeStylesSynchronization: CodeStylesSynchronizationConfiguration? = null

    @JvmField var symfonyProfiler: SymfonyProfilerConfiguration? = null

    @JvmField var classStyles: ClassStylesConfiguration? = null

    @JvmField var phpRunner: PhpRunnerConfiguration? = null
}
