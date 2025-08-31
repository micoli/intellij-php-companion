package org.micoli.php.configuration.documentation;

import org.micoli.php.configuration.models.Configuration;

public class ConfigurationGenerator {

    private final InstanceGenerator generator = new InstanceGenerator();

    public Configuration generateDefaultConfiguration(boolean useExampleAsDefaultValue) {
        return generator.get(Configuration.class, useExampleAsDefaultValue);
    }

    public <T> T generateConfiguration(Class<T> configClass, boolean useExampleAsDefaultValue) {
        return generator.get(configClass, useExampleAsDefaultValue);
    }
}
