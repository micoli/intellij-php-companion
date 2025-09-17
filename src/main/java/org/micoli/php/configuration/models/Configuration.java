package org.micoli.php.configuration.models;

import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration;
import org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration;
import org.micoli.php.consoleCleaner.configuration.ConsoleCleanerConfiguration;
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration;
import org.micoli.php.peerNavigation.configuration.PeerNavigationConfiguration;
import org.micoli.php.symfony.list.configuration.CommandsConfiguration;
import org.micoli.php.symfony.list.configuration.DoctrineEntitiesConfiguration;
import org.micoli.php.symfony.list.configuration.OpenAPIConfiguration;
import org.micoli.php.symfony.list.configuration.RoutesConfiguration;
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration;
import org.micoli.php.tasks.configuration.TasksConfiguration;

public final class Configuration {
    public PeerNavigationConfiguration peerNavigation;
    public SymfonyMessengerConfiguration symfonyMessenger;
    public AttributeNavigationConfiguration attributeNavigation;
    public ExportSourceToMarkdownConfiguration exportSourceToMarkdown;
    public RoutesConfiguration routesConfiguration;
    public CommandsConfiguration commandsConfiguration;
    public DoctrineEntitiesConfiguration doctrineEntitiesConfiguration;
    public ConsoleCleanerConfiguration consoleCleaner;
    public OpenAPIConfiguration openAPIConfiguration;
    public TasksConfiguration tasksConfiguration;
    public CodeStylesSynchronizationConfiguration codeStylesSynchronization;
}
