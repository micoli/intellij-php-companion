package org.micoli.php.configuration;

public class ConfigurationException extends Exception {
    public final Long serial;
    public final String descriptorString;
    public final String originalContent;

    public ConfigurationException(String message, Long serial, String descriptorString, String originalContent) {
        super(message);
        this.serial = serial;
        this.descriptorString = descriptorString;
        this.originalContent = originalContent;
    }
}
