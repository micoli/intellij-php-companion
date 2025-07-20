package org.micoli.php.symfony.list;

import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.service.PhpUtil;
import org.micoli.php.service.attributes.AttributeMapping;
import org.micoli.php.symfony.list.configuration.RoutesConfiguration;

public class RouteService {
    private Project project;
    private RoutesConfiguration configuration = null;
    AttributeMapping mapping = new AttributeMapping(new LinkedHashMap<>() {
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

    public static RouteService getInstance(Project project) {
        return project.getService(RouteService.class);
    }

    public RoutesConfiguration getConfiguration() {
        return configuration;
    }

    public List<RouteElementDTO> getRoutes() {
        if (this.configuration == null) {
            return null;
        }
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        List<RouteElementDTO> routes = new ArrayList<>();
        for (String namespace : this.configuration.namespaces) {
            Collection<String> allClasses = phpIndex.getAllClassFqns(new PlainPrefixMatcher(namespace));
            for (String className : allClasses) {
                PhpClass phpClass = PhpUtil.getPhpClassByFQN(project, PhpUtil.normalizeNonRootFQN(className));
                if (phpClass == null) {
                    continue;
                }
                String attributeFQCN = PhpUtil.normalizeRootFQN(configuration.attributeFQCN);
                routes.addAll(getRoutesFromAttributes(phpClass.getAttributes(attributeFQCN)));
                for (Method method : phpClass.getMethods()) {
                    routes.addAll(getRoutesFromAttributes(method.getAttributes(attributeFQCN)));
                }
            }
        }

        return routes;
    }

    private List<RouteElementDTO> getRoutesFromAttributes(Collection<PhpAttribute> attributes) {
        List<RouteElementDTO> routes = new ArrayList<>();
        for (PhpAttribute attribute : attributes) {
            Map<String, String> values = mapping.clone().extractValues(attribute);
            routes.add(new RouteElementDTO(values.get("path"), values.get("name"), values.get("methods"), attribute));
        }
        return routes;
    }

    private static @NotNull String extractMethods(PhpAttribute.PhpAttributeArgument attributeArgument) {
        return attributeArgument.getArgument().getValue().replaceAll("[\"'\\[\\]]", "");
    }

    private static @NotNull String getStringableValue(PhpAttribute.PhpAttributeArgument attributeArgument) {
        return attributeArgument.getArgument().getValue().replaceAll("^[\"']|[\"']$", "");
    }

    public void loadConfiguration(Project project, RoutesConfiguration configuration) {
        this.project = project;
        this.configuration = configuration;
    }
}
