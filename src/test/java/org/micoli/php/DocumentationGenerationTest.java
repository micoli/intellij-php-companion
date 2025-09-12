package org.micoli.php;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import junit.framework.TestCase;
import org.junit.Assert;
import org.micoli.php.configuration.documentation.*;
import org.micoli.php.configuration.models.Configuration;

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
            | Property                       | Description              |
            | ------------------------------ | ------------------------ |
            | aSubConfiguration[]            |                          |
            | aSubConfiguration[].aProperty1 | description of property1 |
            | aSubConfiguration[].aProperty2 |                          |
            | aBooleanValue                  | description of property1 |

            - **aSubConfiguration[]**
            - **aSubConfiguration[].aProperty1**
              - description of property1
              - **Example**: ``` example value of property1 ```
              - **Default Value**: ``` default value of property1 ```
            - **aSubConfiguration[].aProperty2**
            - **aBooleanValue**
              - description of property1
              - **Example**: ``` example value of property1 ```
              - **Default Value**: ``` false ```
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

    public void testItGeneratesExampleAsJson() throws JsonProcessingException {
        Object example = (new InstanceGenerator()).get(Configuration.class, true);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValueAsString(example);
    }

    public void testItGeneratesDescriptionProperties() {
        MarkdownSchemaGenerator markdownSchemaGenerator = new MarkdownSchemaGenerator();
        markdownSchemaGenerator.generateMarkdownExample(Configuration.class, "root");
    }

    public void testItGeneratesDescriptionExample() {
        MarkdownSchemaGenerator markdownSchemaGenerator = new MarkdownSchemaGenerator();
        markdownSchemaGenerator.generateMarkdownProperties(Configuration.class);
    }

    public void testItVerifyReadmeMdIsUpToDate() throws IOException {
        MarkdownProcessor processor = new MarkdownProcessor();
        String readmeMdPath = "./README.md";
        String existingReadMeContent = Files.readString(Path.of(readmeMdPath));
        String newContent = processor.processFile(readmeMdPath);
        Assert.assertEquals(existingReadMeContent, newContent);
    }
}
