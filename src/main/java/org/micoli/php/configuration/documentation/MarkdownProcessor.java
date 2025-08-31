package org.micoli.php.configuration.documentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownProcessor {

    private static final Pattern INCLUDE_PATTERN = Pattern.compile(
            "(?<startTag><!--\\s*includeDocumentation\\(\"(?<className>[^\"]+)\",\"(?<exportType>[^\"]+)\",\"(?<extraArgument>[^\"]*)\"\\)\\s*-->)"
                    + "(?<oldContent>.*?)"
                    + "(?<endTag><!--\\s*includeDocumentationEnd\\s*-->)",
            Pattern.DOTALL);

    public String processFile(String filePath) throws IOException {
        return processContent(Files.readString(Path.of(filePath)));
    }

    public void processAndSaveFile(String inputPath, String outputPath) throws IOException {
        Files.writeString(Path.of(outputPath), processFile(inputPath));
    }

    public String processContent(String content) {
        Matcher matcher = INCLUDE_PATTERN.matcher(content);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String startTag = matcher.group("startTag");
            String endTag = matcher.group("endTag");
            String className = matcher.group("className");
            String exportType = matcher.group("exportType");
            String extraArgument = matcher.group("extraArgument");
            //            for (Map.Entry<String, Integer> a : matcher.namedGroups().entrySet()) {
            //                System.out.println(a.getKey() + ": [[[" + matcher.group(a.getValue()) + "]]]");
            //            }

            String replacement = String.format(
                    "%s\n%s\n%s", startTag, generateDocumentation(className, exportType, extraArgument), endTag);

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String generateDocumentation(String className, String type, String extraArgument) {
        MarkdownSchemaGenerator markdownSchemaGenerator = new MarkdownSchemaGenerator();
        try {
            Class<?> clazz = Class.forName(className);

            return switch (type) {
                case "example" -> markdownSchemaGenerator.generateMarkdownDocumentation(
                        DocumentationType.EXAMPLE, clazz, 3, extraArgument);
                case "properties" -> markdownSchemaGenerator.generateMarkdownDocumentation(
                        DocumentationType.PROPERTIES, clazz, 1, extraArgument);
                case "description" -> markdownSchemaGenerator.generateMarkdownDocumentation(
                        DocumentationType.DESCRIPTION, clazz, 1, extraArgument);
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
