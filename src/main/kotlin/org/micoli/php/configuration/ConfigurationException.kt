package org.micoli.php.configuration

class ConfigurationException(
    message: String,
    @JvmField val serial: Long,
    val descriptorString: String,
    val originalContent: String?,
) : Exception(message)
