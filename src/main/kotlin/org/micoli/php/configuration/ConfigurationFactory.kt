package org.micoli.php.configuration

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Files
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import kotlin.Any
import kotlin.Boolean
import kotlin.Exception
import kotlin.IllegalStateException
import kotlin.Throws
import org.micoli.php.configuration.GsonTools.ConflictStrategy
import org.micoli.php.configuration.GsonTools.JsonObjectExtensionConflictException
import org.micoli.php.configuration.exceptions.JsonExceptionMapper
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.configuration.models.Configuration
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.scanner.ScannerException

class ConfigurationFactory {
    @JvmField
    var acceptableConfigurationFilesGlob: String = "glob:**/.php-companion{,.local}.{json,yaml}"

    @JvmField
    val acceptableConfigurationFiles: MutableList<String> =
        mutableListOf(
            ".php-companion.json",
            ".php-companion.yaml",
            ".php-companion.local.json",
            ".php-companion.local.yaml",
        )

    data class LoadedConfiguration(val configuration: Configuration?, val timestamp: Long?) {
        var ignoredProperties: MutableList<String> = ArrayList()
    }

    @Throws(ConfigurationException::class, NoConfigurationFileException::class)
    fun loadConfiguration(
        projectPath: String?,
        previousLatestFileTimestampUpdate: Long,
        force: Boolean,
    ): LoadedConfiguration? {
        val files =
            acceptableConfigurationFiles
                .stream()
                .filter { configurationFile: String ->
                    File(projectPath, configurationFile).exists()
                }
                .toList()
        if (files.isEmpty()) {
            throw NoConfigurationFileException(
                "No .php-companion(.local).(json|yaml) configuration file(s) found.", 0L)
        }
        val latestFileUpdateTimestamp = getLatestFileTimestampUpdate(projectPath, files)
        if (previousLatestFileTimestampUpdate == latestFileUpdateTimestamp && !force) {
            return null
        }
        val objectMapper = ObjectMapper()
        val propertiesHandler = IgnoredPropertiesHandler(mutableListOf(Configuration::class.java))
        objectMapper.addHandler(propertiesHandler)
        var stringContent: String? = ""
        try {
            stringContent = loadConfigurationFiles(projectPath, files)
            val configuration = objectMapper.readValue(stringContent, Configuration::class.java)
            val loadedConfiguration = LoadedConfiguration(configuration, latestFileUpdateTimestamp)
            val unknownProperties = propertiesHandler.getUnknownProperties()
            if (!unknownProperties.isEmpty()) {
                throw ConfigurationException(
                    "Unrecognized Property: " + unknownProperties.joinToString(", "),
                    latestFileUpdateTimestamp,
                    "",
                    stringContent,
                )
            }
            loadedConfiguration.ignoredProperties = propertiesHandler.getIgnoredProperties()
            return loadedConfiguration
        } catch (configurationException: ConfigurationException) {
            throw ConfigurationException(
                configurationException.message,
                latestFileUpdateTimestamp,
                configurationException.descriptorString,
                configurationException.originalContent,
            )
        } catch (mappingException: JsonMappingException) {
            throw ConfigurationException(
                (JsonExceptionMapper.getExceptionName(mappingException) +
                    ": " +
                    getReadablePathReference(mappingException)),
                latestFileUpdateTimestamp,
                mappingException.message,
                stringContent,
            )
        } catch (exception: Exception) {
            throw ConfigurationException(
                exception.message,
                latestFileUpdateTimestamp,
                exception.javaClass.descriptorString(),
                stringContent,
            )
        }
    }

    private fun getReadablePathReference(mappingException: JsonMappingException): String {
        return mappingException.path
            .stream()
            .map { pathReference: JsonMappingException.Reference? ->
                if (pathReference!!.index == -1) {
                    return@map pathReference.fieldName
                }
                if (pathReference.fieldName == null) {
                    return@map "[" + pathReference.index + "]"
                }
                pathReference.fieldName + "[" + pathReference.index + "]"
            }
            .collect(Collectors.joining("."))
    }

    @Throws(
        ConfigurationException::class,
        IOException::class,
        JsonObjectExtensionConflictException::class)
    private fun loadConfigurationFiles(projectPath: String?, files: MutableList<String>): String {
        val mergedJson = JsonObject()
        for (file in files) {
            try {
                val configurationSource = getConfigurationSource(file, File(projectPath, file))
                val asJsonObject = JsonParser.parseString(configurationSource).asJsonObject
                GsonTools.extendJsonObject(
                    mergedJson, ConflictStrategy.PREFER_SECOND_OBJ, asJsonObject)
            } catch (e: IllegalStateException) {
                if (!e.localizedMessage.contains("Not a JSON Object: null")) {
                    throw e
                }
            }
        }
        return mergedJson.toString()
    }

    @Throws(ConfigurationException::class, IOException::class)
    private fun getConfigurationSource(file: String, fullPathFile: File): String {
        if (file.endsWith(".json")) {
            return Files.asCharSource(fullPathFile, StandardCharsets.UTF_8).read()
        }
        try {
            val load = Yaml().load<Any?>(FileReader(fullPathFile))
            return GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(load, LinkedHashMap::class.java)
        } catch (e: ScannerException) {
            throw ConfigurationException(
                e.problem + "\n" + e.problemMark.toString(), null, e.message, file)
        } catch (e: FileNotFoundException) {
            throw ConfigurationException(e.message, null, e.message, file)
        }
    }

    private fun getLatestFileTimestampUpdate(
        projectPath: String?,
        files: MutableList<String>
    ): Long {
        var max = 0L
        for (file in files) {
            max = max.coerceAtLeast(File(projectPath, file).lastModified())
        }
        return max
    }
}
