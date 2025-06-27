package org.micoli.php.configuration;

public class ConfigurationException extends Exception {
    public final Long serial;

    public ConfigurationException(String message, Long serial) {
        super(message);
        this.serial = serial;
    }
}
