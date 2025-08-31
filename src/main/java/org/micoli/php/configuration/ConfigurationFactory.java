package org.micoli.php.configuration;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.micoli.php.configuration.exceptions.JsonExceptionMapper;
import org.micoli.php.configuration.exceptions.NoConfigurationFileException;
import org.micoli.php.configuration.models.Configuration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

public class ConfigurationFactory {

    public static class LoadedConfiguration {
        public Configuration configuration;
        public Long timestamp;
        public List<String> ignoredProperties = new ArrayList<>();

        private LoadedConfiguration(Configuration configuration, Long timestamp) {
            this.configuration = configuration;
            this.timestamp = timestamp;
        }
    }

    public static final List<String> acceptableConfigurationFiles = List.of(
            ".php-companion.json", ".php-companion.yaml", ".php-companion.local.json", ".php-companion.local.yaml");

    public static LoadedConfiguration loadConfiguration(String projectPath, Long previousLatestFileTimestampUpdate)
            throws ConfigurationException, NoConfigurationFileException {
        List<String> files = acceptableConfigurationFiles.stream()
                .filter((configurationFile) -> new File(projectPath, configurationFile).exists())
                .toList();
        if (files.isEmpty()) {
            throw new NoConfigurationFileException(
                    "No .php-companion(.local).(json|yaml) configuration file(s) found.", 0L);
        }
        long latestFileUpdateTimestamp = getLatestFileTimestampUpdate(projectPath, files);
        if (previousLatestFileTimestampUpdate == latestFileUpdateTimestamp) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        IgnoredPropertiesHandler propertiesHandler = new IgnoredPropertiesHandler(List.of(Configuration.class));
        objectMapper.addHandler(propertiesHandler);
        String stringContent = "";
        try {
            stringContent = loadConfigurationFiles(projectPath, files);
            Configuration configuration = objectMapper.readValue(stringContent, Configuration.class);
            LoadedConfiguration loadedConfiguration = new LoadedConfiguration(configuration, latestFileUpdateTimestamp);
            List<String> unknownProperties = propertiesHandler.getUnknownProperties();
            if (!unknownProperties.isEmpty()) {
                throw new ConfigurationException(
                        "Unrecognized Property: " + String.join(", ", unknownProperties),
                        latestFileUpdateTimestamp,
                        "",
                        stringContent);
            }
            loadedConfiguration.ignoredProperties = propertiesHandler.getIgnoredProperties();
            return loadedConfiguration;
        } catch (ConfigurationException configurationException) {
            throw new ConfigurationException(
                    configurationException.getMessage(),
                    latestFileUpdateTimestamp,
                    configurationException.descriptorString,
                    configurationException.originalContent);
        } catch (JsonMappingException mappingException) {
            throw new ConfigurationException(
                    JsonExceptionMapper.getExceptionName(mappingException) + ": "
                            + getReadablePathReference(mappingException),
                    latestFileUpdateTimestamp,
                    mappingException.getMessage(),
                    stringContent);
        } catch (Exception exception) {
            throw new ConfigurationException(
                    exception.getMessage(),
                    latestFileUpdateTimestamp,
                    exception.getClass().descriptorString(),
                    stringContent);
        }
    }

    private static String getReadablePathReference(JsonMappingException mappingException) {

        return mappingException.getPath().stream()
                .map((pathReference) -> {
                    if (pathReference.getIndex() == -1) {
                        return pathReference.getFieldName();
                    }
                    if (pathReference.getFieldName() == null) {
                        return "[" + pathReference.getIndex() + "]";
                    }
                    return pathReference.getFieldName() + "[" + pathReference.getIndex() + "]";
                })
                .collect(Collectors.joining("."));
    }

    private static String loadConfigurationFiles(String projectPath, List<String> files)
            throws ConfigurationException, IOException, GsonTools.JsonObjectExtensionConflictException {
        JsonObject mergedJson = new JsonObject();
        for (String file : files) {

            try {
                String configurationSource = getConfigurationSource(file, new File(projectPath, file));
                JsonObject asJsonObject =
                        JsonParser.parseString(configurationSource).getAsJsonObject();
                GsonTools.extendJsonObject(mergedJson, GsonTools.ConflictStrategy.PREFER_SECOND_OBJ, asJsonObject);
            } catch (IllegalStateException e) {
                if (!e.getMessage().contains("Not a JSON Object: null")) {
                    throw e;
                }
            }
        }
        return mergedJson.toString();
    }

    private static String getConfigurationSource(String file, File fullPathFile)
            throws ConfigurationException, IOException {
        if (file.endsWith(".json")) {
            return Files.asCharSource(fullPathFile, StandardCharsets.UTF_8).read();
        }
        try {
            final Object load = new Yaml().load(new FileReader(fullPathFile));
            return new GsonBuilder().setPrettyPrinting().create().toJson(load, LinkedHashMap.class);
        } catch (ScannerException e) {
            throw new ConfigurationException(
                    e.getProblem() + "\n" + e.getProblemMark().toString(), null, e.getMessage(), file);
        } catch (FileNotFoundException e) {
            throw new ConfigurationException(e.getMessage(), null, e.getMessage(), file);
        }
    }

    private static long getLatestFileTimestampUpdate(String projectPath, List<String> files) {
        long max = 0L;
        for (String file : files) {
            max = Long.max(max, new File(projectPath, file).lastModified());
        }
        return max;
    }
}
