package org.micoli.php;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.micoli.php.configuration.models.Configuration;
import org.micoli.php.configuration.schema.ConfigurationJsonSchemaGenerator;
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
