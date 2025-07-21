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
import org.micoli.php.symfony.list.configuration.CommandsConfiguration;

public class CommandService {
    private Project project;
    private CommandsConfiguration configuration = null;
    AttributeMapping mapping = new AttributeMapping(new LinkedHashMap<>() {
        {
            put("name", CommandService::getStringableValue);
            put("description", CommandService::getStringableValue);
            put("aliases", CommandService::getStringableValue);
        }
    });

    public static CommandService getInstance(Project project) {
        return project.getService(CommandService.class);
    }

    public CommandsConfiguration getConfiguration() {
        return configuration;
    }

    public List<CommandElementDTO> getCommands() {
        if (this.configuration == null) {
            return null;
        }
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        List<CommandElementDTO> routes = new ArrayList<>();
        for (String namespace : this.configuration.namespaces) {
            Collection<String> allClasses = phpIndex.getAllClassFqns(new PlainPrefixMatcher(namespace));
            for (String className : allClasses) {
                PhpClass phpClass = PhpUtil.getPhpClassByFQN(project, PhpUtil.normalizeNonRootFQN(className));
                if (phpClass == null) {
                    continue;
                }
                String attributeFQCN = PhpUtil.normalizeRootFQN(configuration.attributeFQCN);
                routes.addAll(getCommandsFromAttributes(phpClass.getAttributes(attributeFQCN)));
                for (Method method : phpClass.getMethods()) {
                    routes.addAll(getCommandsFromAttributes(method.getAttributes(attributeFQCN)));
                }
            }
        }

        return routes;
    }

    private List<CommandElementDTO> getCommandsFromAttributes(Collection<PhpAttribute> attributes) {
        List<CommandElementDTO> commands = new ArrayList<>();
        for (PhpAttribute attribute : attributes) {
            Map<String, String> values = mapping.clone().extractValues(attribute);
            commands.add(new CommandElementDTO(values.get("name"), values.get("description"), attribute));
        }
        return commands;
    }

    private static @NotNull String getStringableValue(PhpAttribute.PhpAttributeArgument attributeArgument) {
        return attributeArgument.getArgument().getValue().replaceAll("^[\"']|[\"']$", "");
    }

    public void loadConfiguration(Project project, CommandsConfiguration configuration) {
        this.project = project;
        this.configuration = configuration;
    }
}
