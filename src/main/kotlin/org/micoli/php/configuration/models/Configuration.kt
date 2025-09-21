package org.micoli.php.configuration.models

import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration
import org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration
import org.micoli.php.consoleCleaner.configuration.ConsoleCleanerConfiguration
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration
import org.micoli.php.peerNavigation.configuration.PeerNavigationConfiguration
import org.micoli.php.symfony.list.configuration.CommandsConfiguration
import org.micoli.php.symfony.list.configuration.DoctrineEntitiesConfiguration
import org.micoli.php.symfony.list.configuration.OpenAPIConfiguration
import org.micoli.php.symfony.list.configuration.RoutesConfiguration
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration
import org.micoli.php.tasks.configuration.TasksConfiguration

class Configuration {
    @JvmField var peerNavigation: PeerNavigationConfiguration? = null

    @JvmField var symfonyMessenger: SymfonyMessengerConfiguration? = null

    @JvmField var attributeNavigation: AttributeNavigationConfiguration? = null

    @JvmField var exportSourceToMarkdown: ExportSourceToMarkdownConfiguration? = null

    @JvmField var routesConfiguration: RoutesConfiguration? = null

    @JvmField var commandsConfiguration: CommandsConfiguration? = null

    @JvmField var doctrineEntitiesConfiguration: DoctrineEntitiesConfiguration? = null

    @JvmField var consoleCleaner: ConsoleCleanerConfiguration? = null

    @JvmField var openAPIConfiguration: OpenAPIConfiguration? = null

    @JvmField var tasksConfiguration: TasksConfiguration? = null

    @JvmField var codeStylesSynchronization: CodeStylesSynchronizationConfiguration? = null
}
