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
    <!-- generateDocumentation%s("org.micoli.php.DocumentationGenerationTest$TestConfiguration","%s") -->
    xxxxx
    <!-- generateDocumentationEnd -->
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
            <!-- generateDocumentationExample("org.micoli.php.DocumentationGenerationTest$TestConfiguration","") -->
            ```yaml
            aBooleanValue: false
            aSubConfiguration:
            - aProperty1: default value of property1
              aProperty2: ''

            ```
            <!-- generateDocumentationEnd -->
            """;

        String result = processor.processContent(String.format(initial, "Example", ""));

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
            <!-- generateDocumentationExample("org.micoli.php.DocumentationGenerationTest$TestConfiguration","exampleRoot") -->
            ```yaml
            exampleRoot:
              aBooleanValue: false
              aSubConfiguration:
              - aProperty1: default value of property1
                aProperty2: ''
            ```
            <!-- generateDocumentationEnd -->
            """;

        String result = processor.processContent(String.format(initial, "Example", "exampleRoot"));

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
            <!-- generateDocumentationProperties("org.micoli.php.DocumentationGenerationTest$TestConfiguration","") -->
            | Property                       | Description              | Example                      | Default value               |
            | ------------------------------ | ------------------------ | ---------------------------- | --------------------------- |
            | aSubConfiguration[]            |                          |                              |                             |
            | aSubConfiguration[].aProperty1 | description of property1 | `example value of property1` | `default value of property1 |
            | aSubConfiguration[].aProperty2 |                          |                              |                             |
            | aBooleanValue                  | description of property1 | `example value of property1` | `false                      |
            <!-- generateDocumentationEnd -->
            """;

        String result = processor.processContent(String.format(initial, "Properties", ""));

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
            <!-- generateDocumentationDescription("org.micoli.php.DocumentationGenerationTest$TestConfiguration","") -->
            description of TestConfiguration
            <!-- generateDocumentationEnd -->
            """;

        String result = processor.processContent(String.format(initial, "Description", ""));

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
