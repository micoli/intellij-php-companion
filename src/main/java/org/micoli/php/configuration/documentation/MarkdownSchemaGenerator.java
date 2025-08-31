package org.micoli.php.configuration.documentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.*;
import java.util.stream.Collectors;
import net.steppschuh.markdowngenerator.table.Table;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class MarkdownSchemaGenerator {

    public String generateMarkdownDescription(DocumentationType type, Class<?> clazz) {

        Schema classSchema = clazz.getAnnotation(Schema.class);
        if (classSchema != null && !classSchema.description().isEmpty()) {
            return classSchema.description();
        }
        return "";
    }

    public String generateMarkdownProperties(DocumentationType type, Class<?> clazz) {
        return getYamlProperties(clazz);
    }

    public String generateMarkdownExample(DocumentationType type, Class<?> clazz, String exampleRoot) {
        return (new StringBuilder())
                .append("```yaml\n")
                .append(generateYamlExample(exampleRoot, clazz).replaceAll("```", "````"))
                .append("\n```")
                .toString();
    }

    private String getYamlProperties(Class<?> clazz) {
        Object example = (new InstanceGenerator()).get(clazz, false);
        Table.Builder tableBuilder = new Table.Builder().addRow("Property", "Description", "Example", "Default value");

        ClassPropertiesDocumentationGenerator classPropertyTraverser = new ClassPropertiesDocumentationGenerator();
        List<ClassPropertiesDocumentationGenerator.PropertyInfo> fields =
                classPropertyTraverser.getProperties(example, 5);
        for (ClassPropertiesDocumentationGenerator.PropertyInfo property : fields) {
            tableBuilder.addRow(
                    property.dotNotationPath(),
                    (property.description() == null || property.description().isEmpty()) ? "" : property.description(),
                    (property.example() == null || property.example().isEmpty()) ? "" : "`" + property.example() + "`",
                    (property.defaultValue() == null || property.defaultValue().isEmpty())
                            ? ""
                            : "`" + property.defaultValue().replaceAll("\n", "\\\\n"));
        }
        return tableBuilder.build().serialize();
    }

    private String generateYamlExample(String exampleRoot, Class<?> clazz) {
        Object example = (new InstanceGenerator()).get(clazz, true);
        DumperOptions options = new DumperOptions();
        options.setExplicitStart(false);
        options.setExplicitEnd(false);
        options.setCanonical(false);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        String yamlContent = yaml.dump(example).replaceAll("!!\\S+\\s*", "");
        if (exampleRoot == null || exampleRoot.isEmpty()) {
            return yamlContent;
        }

        return String.format(
                "%s:\n%s",
                exampleRoot,
                Arrays.stream(yamlContent.split("\n")).map(str -> "  " + str).collect(Collectors.joining("\n")));
    }
}
