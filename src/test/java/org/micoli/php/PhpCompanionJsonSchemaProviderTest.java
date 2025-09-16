package org.micoli.php;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.htmlInspections.RequiredAttributesInspection;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.jsonSchema.JsonSchemaMappingsProjectConfiguration;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import com.jetbrains.jsonSchema.ide.JsonSchemaService;
import com.jetbrains.jsonSchema.impl.inspections.JsonSchemaComplianceInspection;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.schema.YamlJsonSchemaHighlightingInspection;
import org.micoli.php.configuration.schema.PhpCompanionJsonSchemaProviderFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class PhpCompanionJsonSchemaProviderTest extends BasePlatformTestCase {

    private final String invalidYamlContent =
            """
            peerNavigation:
              peers:
                - source: 123  # Error: should be a string
                  target: App\\Repository\\*Repository
              associates:
                - classA: App\\Entity\\User
                  invalidProperty: "This should not exist"  # invalid property
            symfonyMessenger:
              projectRootNamespace: true  # Error: should be a string
              messageInterfaces: "not an array"  # Error: should be an array
            """;

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        InspectionProfileImpl.INIT_INSPECTIONS = true;
        myFixture.enableInspections(new LocalInspectionTool[] {
            new RequiredAttributesInspection(),
            new JsonSchemaComplianceInspection(),
            new YamlJsonSchemaHighlightingInspection(),
        });
    }

    public void testInvalidYamlConfiguration() {
        VirtualFile yamlFile = myFixture
                .configureByText(".php-companion.yaml", invalidYamlContent)
                .getVirtualFile();

        Collection<VirtualFile> schemaFiles =
                JsonSchemaService.Impl.get(getProject()).getSchemaFilesForFile(yamlFile);
        assertFalse("Schema must be found here", schemaFiles.isEmpty());
        assertTrue(
                "File must be accepted by the provider",
                new PhpCompanionJsonSchemaProviderFactory.PhpCompanionJsonSchemaProvider().isAvailable(yamlFile));
    }

    public void testIfProviderIsWiredCorrectly() {
        JsonSchemaProviderFactory factory = new PhpCompanionJsonSchemaProviderFactory();
        List<JsonSchemaFileProvider> providers = factory.getProviders(getProject());
        assertFalse("At least a provider must be provided", providers.isEmpty());

        JsonSchemaService service = JsonSchemaService.Impl.get(getProject());
        assertNotNull("A JSON schema service should be available", service);

        JsonSchemaMappingsProjectConfiguration mappings =
                JsonSchemaMappingsProjectConfiguration.getInstance(getProject());
        assertNotNull("Mappings should be available", mappings);
    }

    public void testItValidateWithExtraProperty() throws IOException {
        myFixture.configureByText(".php-companion.yaml", """
            aa: azeerty
            """);
        List<HighlightInfo> highlights = myFixture.doHighlighting();
        assertNotEmpty(highlights);
    }

    public void testItValidateConfigurationInvalidFile() {
        myFixture.configureByText(".php-companion.yaml", invalidYamlContent);

        List<HighlightInfo> highlights = myFixture.doHighlighting();
        assertEquals(
                """
            2: - source: 123  # Error: should be a string [123] Schema validation: Incompatible types.
             Required: string. Actual: integer.
            8: projectRootNamespace: true  # Error: should be a string [true] Schema validation: Incompatible types.
             Required: string. Actual: boolean.
            9: messageInterfaces: "not an array"  # Error: should be an array ["not an array"] Schema validation: Incompatible types.
             Required: array. Actual: string.
            """
                        .trim(),
                formatHighlights(myFixture.getEditor().getDocument(), highlights)
                        .trim());
    }

    private @NotNull String formatHighlights(Document document, List<HighlightInfo> highlights) {
        return highlights.stream()
                .map(i -> {
                    int lineNumber = document.getLineNumber(i.getStartOffset());
                    int lineStart = document.getLineStartOffset(lineNumber);
                    int lineEnd = document.getLineEndOffset(lineNumber);
                    String lineContent =
                            document.getText(new TextRange(lineStart, lineEnd)).trim();

                    return String.format("%s: %s [%s] %s", lineNumber, lineContent, i.getText(), i.getDescription());
                })
                .collect(Collectors.joining("\n"));
    }

    public void testItGeneratesProperSchema() throws IOException {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setIndent(2);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        new Yaml(dumperOptions).dump(new ObjectMapper().readValue(generateJsonSchemaThoughProvider(), Object.class));
    }

    private @NotNull String generateJsonSchemaThoughProvider() throws IOException {
        return VfsUtilCore.loadText(Objects.requireNonNull(new PhpCompanionJsonSchemaProviderFactory()
                .getProviders(getProject())
                .getFirst()
                .getSchemaFile()));
    }
}
