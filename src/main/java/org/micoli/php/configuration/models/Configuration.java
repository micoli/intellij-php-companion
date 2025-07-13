package org.micoli.php.configuration.models;

import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration;
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration;
import org.micoli.php.peerNavigation.configuration.PeerNavigationConfiguration;
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration;

public final class Configuration {
    public PeerNavigationConfiguration peerNavigation;
    public SymfonyMessengerConfiguration symfonyMessenger;
    public AttributeNavigationConfiguration attributeNavigation;
    public ExportSourceToMarkdownConfiguration exportSourceToMarkdown;
}
