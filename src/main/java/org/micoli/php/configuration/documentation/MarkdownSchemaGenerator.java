package org.micoli.php.configuration.documentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.*;
import java.util.stream.Collectors;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.table.Table;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class MarkdownSchemaGenerator {

    public String generateMarkdownDescription(Class<?> clazz) {

        Schema classSchema = clazz.getAnnotation(Schema.class);
        if (classSchema != null && !classSchema.description().isEmpty()) {
            return classSchema.description();
        }
        return "";
    }

    public String generateMarkdownProperties(Class<?> clazz) {
        return getYamlProperties(clazz);
    }

    public String generateMarkdownExample(Class<?> clazz, String exampleRoot) {
        return "```yaml\n" + generateYamlExample(exampleRoot, clazz).replaceAll("```", "````") + "\n```";
    }

    private String getYamlProperties(Class<?> clazz) {
        Object example = (new InstanceGenerator()).get(clazz, false);
        Table.Builder tableBuilder = new Table.Builder().addRow("Property", "Description");

        ClassPropertiesDocumentationGenerator classPropertyTraverser = new ClassPropertiesDocumentationGenerator();
        List<ClassPropertiesDocumentationGenerator.PropertyInfo> fields =
                classPropertyTraverser.getProperties(example, 5);
        List<Object> items = new ArrayList<>();
        for (ClassPropertiesDocumentationGenerator.PropertyInfo property : fields) {
            tableBuilder.addRow(
                    property.dotNotationPath(),
                    (property.description() == null || property.description().isEmpty()) ? "" : property.description());
            items.add(new UnorderedList<>(getProperties(property)));
        }

        return tableBuilder.build().serialize() + "\n\n"
                + unindentYamlLines(new UnorderedList<>(items).toString(), "^  ");
    }

    private static @NotNull List<Object> getProperties(ClassPropertiesDocumentationGenerator.PropertyInfo property) {
        List<String> detailList = new ArrayList<>();
        if (property.description() != null && !property.description().isEmpty()) {
            detailList.add(property.description());
        }
        if (property.example() != null && !property.example().isEmpty()) {
            detailList.add(
                    String.format("**Example**: ``` %s ```", property.example().replaceAll("```", "````")));
        }

        if (property.defaultValue() != null && !property.defaultValue().isEmpty()) {
            detailList.add(String.format(
                    "**Default Value**: ``` %s ```", property.defaultValue().replaceAll("```", "````")));
        }
        return detailList.isEmpty()
                ? List.of("**" + property.dotNotationPath() + "**")
                : Arrays.asList("**" + property.dotNotationPath() + "**", new UnorderedList<>(detailList));
    }

    private String generateYamlExample(String exampleRoot, Class<?> clazz) {
        String yamlContent = dumpYaml(new InstanceGenerator().get(clazz, true));

        if (exampleRoot == null || exampleRoot.isEmpty()) {
            return yamlContent;
        }

        return String.format("%s:\n%s", exampleRoot, indentYamlLines(yamlContent, "  "));
    }

    private static String dumpYaml(Object example) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            DumperOptions options = new DumperOptions();
            options.setExplicitStart(false);
            options.setExplicitEnd(false);
            options.setCanonical(false);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            return new Yaml(options).dump(mapper.readValue(mapper.writeValueAsString(example), TreeMap.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull String indentYamlLines(String yamlContent, String indentation) {
        return Arrays.stream(yamlContent.split("\n"))
                .map(str -> indentation + str)
                .collect(Collectors.joining("\n"));
    }

    private static @NotNull String unindentYamlLines(String yamlContent, String indentation) {
        return Arrays.stream(yamlContent.split("\n"))
                .map(str -> str.replaceFirst(indentation, ""))
                .collect(Collectors.joining("\n"));
    }
}
