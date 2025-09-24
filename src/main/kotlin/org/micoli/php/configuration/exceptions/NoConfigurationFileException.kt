package org.micoli.php.configuration.exceptions

class NoConfigurationFileException(message: String?, @JvmField val serial: Long?) :
    Exception(message)
