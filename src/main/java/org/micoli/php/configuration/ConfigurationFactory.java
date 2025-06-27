package org.micoli.php.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import org.micoli.php.configuration.models.Configuration;
import org.yaml.snakeyaml.Yaml;

public class ConfigurationFactory {
    public static class LoadedConfiguration {
        public Configuration configuration;
        public Long timestamp;

        private LoadedConfiguration(Configuration configuration, Long timestamp) {
            this.configuration = configuration;
            this.timestamp = timestamp;
        }
    }

    public static final ArrayList<String> acceptableConfigurationFiles = new ArrayList<>(Arrays.asList(".php-companion.json", ".php-companion.yaml", ".php-companion.local.json", ".php-companion.local.yaml"));

    public static LoadedConfiguration loadConfiguration(String projectPath, Long previousLatestFileTimestampUpdate) throws ConfigurationException, NoConfigurationFileException {
        List<String> files = acceptableConfigurationFiles.stream().filter((configurationFile) -> new File(projectPath, configurationFile).exists()).toList();
        if (files.isEmpty()) {
            throw new NoConfigurationFileException("No .php-companion(.*).json configuration file(s) found.", 0L);
        }
        long latestFileUpdateTimestamp = getLatestFileTimestampUpdate(projectPath, files);
        if (previousLatestFileTimestampUpdate == latestFileUpdateTimestamp) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        String stringContent = "";
        try {
            stringContent = loadConfigurationFiles(projectPath, files);
            return new LoadedConfiguration(objectMapper.readValue(stringContent, Configuration.class), latestFileUpdateTimestamp);
        } catch (Exception e) {
            throw new ConfigurationException(e.getClass().descriptorString() + "-" + e.getMessage() + "\\n" + stringContent, latestFileUpdateTimestamp);
        }
    }

    private static String loadConfigurationFiles(String projectPath, List<String> files) throws IOException, GsonTools.JsonObjectExtensionConflictException {
        JsonObject mergedJson = new JsonObject();
        final Yaml yaml = new Yaml();
        for (String file : files) {
            String inputBuffer;
            File fullPathFile = new File(projectPath, file);
            if (file.endsWith(".json")) {
                inputBuffer = Files.asCharSource(fullPathFile, StandardCharsets.UTF_8).read();
            }
            else {
                try {
                    final Object load = yaml.load(new FileReader(fullPathFile));
                    inputBuffer = new GsonBuilder().setPrettyPrinting().create().toJson(load, LinkedHashMap.class);
                } catch (Exception e) {
                    throw new IOException(e.getMessage());
                }
            }
            JsonElement jsonFile = JsonParser.parseString(inputBuffer).getAsJsonObject();
            GsonTools.extendJsonObject(mergedJson, GsonTools.ConflictStrategy.PREFER_SECOND_OBJ, jsonFile.getAsJsonObject());
        }
        return mergedJson.toString();
    }

    private static long getLatestFileTimestampUpdate(String projectPath, List<String> files) {
        long max = 0L;
        for (String file : files) {
            max = Long.max(max, new File(projectPath, file).lastModified());
        }
        return max;
    }
}
