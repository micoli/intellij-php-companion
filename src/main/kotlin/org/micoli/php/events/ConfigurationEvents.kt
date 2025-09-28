package org.micoli.php.events

import com.intellij.util.messages.Topic
import org.micoli.php.configuration.models.Configuration

interface ConfigurationEvents {
    fun configurationLoaded(loadedConfiguration: Configuration)

    companion object {
        @JvmField
        val CONFIGURATION_UPDATED: Topic<ConfigurationEvents> =
            Topic.create<ConfigurationEvents>(
                "PHP Companion Configuration Events", ConfigurationEvents::class.java)
    }
}
