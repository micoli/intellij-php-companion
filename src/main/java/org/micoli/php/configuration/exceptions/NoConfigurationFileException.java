package org.micoli.php.configuration.exceptions;

public class NoConfigurationFileException extends Exception {
    public final Long serial;

    public NoConfigurationFileException(String message, Long serial) {
        super(message);
        this.serial = serial;
    }
}
