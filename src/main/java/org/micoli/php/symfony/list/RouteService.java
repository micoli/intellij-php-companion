package org.micoli.php.symfony.list;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.service.attributes.AttributeMapping;
import org.micoli.php.symfony.list.configuration.RoutesConfiguration;

public class RouteService extends AbstractAttributeService<RouteElementDTO, RoutesConfiguration> {
    public RouteService() {
        mapping = new AttributeMapping(new LinkedHashMap<>() {
            {
                put("path", RouteService::getStringableValue);
                put("name", RouteService::getStringableValue);
                put("requirements", RouteService::getStringableValue);
                put("options", RouteService::getStringableValue);
                put("defaults", RouteService::getStringableValue);
                put("host", RouteService::getStringableValue);
                put("methods", RouteService::extractMethods);
            }
        });
    }

    @Override
    protected RouteElementDTO createElementDTO(String className, PhpAttribute attribute, String namespace) {
        Map<String, String> values = mapping.clone().extractValues(attribute);
        return new RouteElementDTO(values.get("path"), values.get("name"), values.get("methods"), className, attribute);
    }

    @Override
    protected String[] getNamespaces() {
        return configuration.namespaces;
    }

    @Override
    protected String getAttributeFQCN() {
        return configuration.attributeFQCN;
    }

    private static @NotNull String extractMethods(PhpAttribute.PhpAttributeArgument attributeArgument) {
        return attributeArgument.getArgument().getValue().replaceAll("[\"'\\[\\]]", "");
    }

    public static RouteService getInstance(Project project) {
        return project.getService(RouteService.class);
    }
}
