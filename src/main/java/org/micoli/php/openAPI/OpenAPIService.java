package org.micoli.php.openAPI;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.exception.EncodingNotSupportedException;
import io.swagger.v3.parser.exception.ReadContentException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.service.attributes.AttributeMapping;
import org.micoli.php.symfony.list.AbstractAttributeService;
import org.micoli.php.symfony.list.configuration.OpenAPIConfiguration;

public class OpenAPIService extends AbstractAttributeService<OpenAPIPathElementDTO, OpenAPIConfiguration> {
    private static final Logger LOG = Logger.getInstance(OpenAPIService.class);

    public OpenAPIService(Project project) {
        super(project);
        mapping = new AttributeMapping(new LinkedHashMap<>() {
            {
                put("uri", OpenAPIService::getStringableValue);
                put("description", OpenAPIService::getStringableValue);
            }
        });
    }

    protected OpenAPIPathElementDTO createElementDTO(String className, PhpAttribute attribute, String namespace) {
        return null;
    }

    @Override
    public List<OpenAPIPathElementDTO> getElements() {
        if (this.configuration == null) {
            return null;
        }
        List<OpenAPIPathElementDTO> elements = new ArrayList<>();
        for (String root : this.configuration.specificationRoots) {
            addElementsFromOpenAPIRoot(root, elements);
        }
        return elements;
    }

    private void addElementsFromOpenAPIRoot(String root, List<OpenAPIPathElementDTO> elements) {
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);
        parseOptions.setResolveCombinators(false);
        final OpenAPI openAPI = new OpenAPIV3Parser().read(project.getBasePath() + "/" + root, null, parseOptions);
        if (openAPI == null) {
            return;
        }
        try {
            Paths paths = openAPI.getPaths();
            for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry :
                        pathItem.readOperationsMap().entrySet()) {
                    PathItem.HttpMethod httpMethod = operationEntry.getKey();
                    Operation operationEntryValue = operationEntry.getValue();
                    String operationId = operationEntryValue.getOperationId();
                    if (operationId == null) {
                        continue;
                    }
                    elements.add(new OpenAPIPathElementDTO(
                            Objects.requireNonNullElse(root, ""),
                            Objects.requireNonNullElse(path, ""),
                            Objects.requireNonNullElse(httpMethod.toString(), ""),
                            Objects.requireNonNullElse(getDescription(pathItem, operationEntryValue), ""),
                            operationId,
                            null));
                }
            }
        } catch (ReadContentException | EncodingNotSupportedException ignored) {
        }
    }

    private static @NotNull String getDescription(PathItem pathItem, Operation operationEntryValue) {
        String pathDescription = Optional.ofNullable(pathItem.getDescription()).orElse("");
        String operationDescription =
                Optional.ofNullable(operationEntryValue.getDescription()).orElse("");

        return Stream.of(pathDescription, operationDescription)
                .filter(desc -> !desc.isEmpty())
                .collect(Collectors.joining(" "))
                .trim();
    }

    @Override
    protected String[] getNamespaces() {
        return null;
    }

    @Override
    protected String getAttributeFQCN() {
        return null;
    }

    public static OpenAPIService getInstance(Project project) {
        return project.getService(OpenAPIService.class);
    }
}
