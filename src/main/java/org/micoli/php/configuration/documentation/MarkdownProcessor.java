package org.micoli.php.configuration.documentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownProcessor {

    private static final Pattern INCLUDE_PATTERN = Pattern.compile(
            "(?<startTag><!--\\s*generateDocumentation(?<exportType>(Description|Example|Properties))\\(\"(?<className>[^\"]+)\",\"(?<extraArgument>[^\"]*)\"\\)\\s*-->)"
                    + "(?<oldContent>.*?)"
                    + "(?<endTag><!--\\s*generateDocumentationEnd\\s*-->)",
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
            String replacement = String.format(
                    "%s\n%s\n%s",
                    matcher.group("startTag"),
                    generateDocumentation(
                            matcher.group("className"), matcher.group("exportType"), matcher.group("extraArgument")),
                    matcher.group("endTag"));

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
                case "Example" -> markdownSchemaGenerator.generateMarkdownExample(clazz, extraArgument);
                case "Properties" -> markdownSchemaGenerator.generateMarkdownProperties(clazz);
                case "Description" -> markdownSchemaGenerator.generateMarkdownDescription(clazz);
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
