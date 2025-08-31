package org.micoli.php;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import junit.framework.TestCase;
import org.junit.Assert;
import org.micoli.php.configuration.documentation.*;

public class DocumentationGenerationTest extends TestCase {
    private final String initial =
            """
    <!-- includeDocumentation("org.micoli.php.DocumentationGenerationTest$TestConfiguration","%s","%s") -->
    xxxxx
    <!-- includeDocumentationEnd -->
    """;

    @Schema(description = "description of SubConfiguration")
    public static final class SubConfiguration {
        @Schema(description = "description of property1", example = "example value of property1")
        public String aProperty1 = "default value of property1";

        public String aProperty2;
    }

    @Schema(description = "description of TestConfiguration")
    public static final class TestConfiguration {
        public SubConfiguration[] aSubConfiguration = new SubConfiguration[] {};

        @Schema(description = "description of property1", example = "example value of property1")
        public boolean aBooleanValue = false;
    }

    public void testItGeneratesExampleWithoutRoot() {
        MarkdownProcessor processor = new MarkdownProcessor();
        String expected =
                """
            <!-- includeDocumentation("org.micoli.php.DocumentationGenerationTest$TestConfiguration","example","") -->
            ```yaml
            aBooleanValue: false
            aSubConfiguration:
            - aProperty1: default value of property1
              aProperty2: ''

            ```
            <!-- includeDocumentationEnd -->
            """;

        String result = processor.processContent(String.format(initial, "example", ""));

        // Then it should change
        Assert.assertEquals(expected, result);

        // Then it should not change twice
        String result2 = processor.processContent(result);
        Assert.assertEquals(result, result2);
    }

    public void testItGeneratesExampleWithRoot() {
        MarkdownProcessor processor = new MarkdownProcessor();
        String expected =
                """
            <!-- includeDocumentation("org.micoli.php.DocumentationGenerationTest$TestConfiguration","example","exampleRoot") -->
            ```yaml
            exampleRoot:
              aBooleanValue: false
              aSubConfiguration:
              - aProperty1: default value of property1
                aProperty2: ''
            ```
            <!-- includeDocumentationEnd -->
            """;

        String result = processor.processContent(String.format(initial, "example", "exampleRoot"));

        // Then it should change
        Assert.assertEquals(expected, result);

        // Then it should not change twice
        String result2 = processor.processContent(result);
        Assert.assertEquals(result, result2);
    }

    public void testItGeneratesProperties() {
        MarkdownProcessor processor = new MarkdownProcessor();
        String expected =
                """
            <!-- includeDocumentation("org.micoli.php.DocumentationGenerationTest$TestConfiguration","properties","") -->
            | Property                       | Description              | Example                      | Default value               |
            | ------------------------------ | ------------------------ | ---------------------------- | --------------------------- |
            | aSubConfiguration[]            |                          |                              |                             |
            | aSubConfiguration[].aProperty1 | description of property1 | `example value of property1` | `default value of property1 |
            | aSubConfiguration[].aProperty2 |                          |                              |                             |
            | aBooleanValue                  | description of property1 | `example value of property1` | `false                      |
            <!-- includeDocumentationEnd -->
            """;

        String result = processor.processContent(String.format(initial, "properties", ""));

        // Then it should change
        Assert.assertEquals(expected, result);

        // Then it should not change twice
        String result2 = processor.processContent(result);
        Assert.assertEquals(result, result2);
    }

    public void testItGeneratesDescription() {
        MarkdownProcessor processor = new MarkdownProcessor();
        String expected =
                """
            <!-- includeDocumentation("org.micoli.php.DocumentationGenerationTest$TestConfiguration","description","") -->
            description of TestConfiguration
            <!-- includeDocumentationEnd -->
            """;

        String result = processor.processContent(String.format(initial, "description", ""));

        // Then it should change
        Assert.assertEquals(expected, result);

        // Then it should not change twice
        String result2 = processor.processContent(result);
        Assert.assertEquals(result, result2);
    }

    public void testItVerifyReadmeMdIsUpToDate() throws IOException {
        MarkdownProcessor processor = new MarkdownProcessor();
        String readmeMdPath = "./README.md";
        String existingReadMeContent = Files.readString(Path.of(readmeMdPath));
        String newContent = processor.processFile(readmeMdPath);
        Assert.assertEquals(existingReadMeContent, newContent);
    }
}
