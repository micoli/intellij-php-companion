package org.micoli.php;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.micoli.php.configuration.models.Configuration;
import org.micoli.php.configuration.schema.ConfigurationJsonSchemaGenerator;
import org.micoli.php.configuration.schema.PhpCompanionJsonSchemaProviderFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class ConfigurationJsonSchemaGeneratorTest extends BasePlatformTestCase {

    public void testItGeneratesProperSchema() throws JsonProcessingException {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setIndent(2);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String yaml = new Yaml(dumperOptions)
                .dump(new ObjectMapper()
                        .readValue(
                                new ConfigurationJsonSchemaGenerator().generateSchema(Configuration.class),
                                Object.class));
        assertTrue(extractMatchesCount(yaml, "([iI]con:\\n\\s*\\$ref: '#/definitions/icons')") >= 9);
        assertTrue(extractMatchesCount(yaml, "(actionId:\\n\\s*\\$ref: '#/definitions/actionIds')") >= 2);
        assertTrue(extractMatchesCount(yaml, "(actionIds:\\n\\s*type: string\\n\\s*enum:\\n\\s*-)") == 1);
        assertTrue(extractMatchesCount(yaml, "(icons:\\n\\s*type: string\\n\\s*enum:\\n\\s*-)") == 1);
        assertTrue(extractMatchesCount(yaml, "(EditorPopupMenu1\\.FindRefactor)") == 1);
        assertTrue(extractMatchesCount(yaml, "(expui\\/actions\\/addFile\\.svg)") == 1);
        assertTrue(extractMatchesCount(yaml, "(anonymous-group-\\d)") == 0);
    }

    public void testIfGeneratedVersionIsIdenticalToResource() throws IOException {
        String expected = new ConfigurationJsonSchemaGenerator().generateSchema(Configuration.class);
        String actual = VfsUtilCore.loadText(Objects.requireNonNull(new PhpCompanionJsonSchemaProviderFactory()
                .getProviders(getProject())
                .getFirst()
                .getSchemaFile()));
        List<String> definitionsToCleanup = List.of("actionIds", "icons");
        assertEquals(
                sanitizeSchemasDefinitions(expected, definitionsToCleanup),
                sanitizeSchemasDefinitions(actual, definitionsToCleanup));
    }

    @SuppressWarnings("unchecked")
    public String sanitizeSchemasDefinitions(String input, List<String> definitionsToCleanup)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        Map<String, Object> value = mapper.readValue(input, Map.class);

        if (value.containsKey("definitions")) {
            Map<String, Object> definitions = (Map<String, Object>) value.get("definitions");
            for (String definitionToCleanup : definitionsToCleanup) {
                if (definitions.containsKey(definitionToCleanup)) {
                    Map<String, Object> definitionNode = (Map<String, Object>) definitions.get(definitionToCleanup);
                    assert (((List<String>) definitionNode.get("enum")).size() > 10);
                    definitionNode.put("enum", new Object[0]);
                }
            }
        }

        return mapper.writeValueAsString(value);
    }

    private int extractMatchesCount(String text, String regex) {
        return extractMatches(text, regex).size();
    }

    private List<String> extractMatches(String text, String regex) {
        List<String> matches = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(text);

        while (matcher.find()) {
            matches.add(matcher.group());
        }

        return matches;
    }
}
