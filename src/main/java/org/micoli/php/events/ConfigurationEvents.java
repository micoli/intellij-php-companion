package org.micoli.php.events;

import com.intellij.util.messages.Topic;
import org.micoli.php.configuration.models.Configuration;

public interface ConfigurationEvents {
    Topic<ConfigurationEvents> CONFIGURATION_UPDATED =
            Topic.create("PHP Companion Configuration Events", ConfigurationEvents.class);

    void configurationLoaded(Configuration configuration);
}
