package org.micoli.php.symfony.list;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import java.util.LinkedHashMap;
import java.util.Map;
import org.micoli.php.service.attributes.AttributeMapping;
import org.micoli.php.symfony.list.configuration.CommandsConfiguration;

public class CommandService extends AbstractAttributeService<CommandElementDTO, CommandsConfiguration> {
    public CommandService() {
        mapping = new AttributeMapping(new LinkedHashMap<>() {
            {
                put("name", CommandService::getStringableValue);
                put("description", CommandService::getStringableValue);
                put("aliases", CommandService::getStringableValue);
            }
        });
    }

    @Override
    protected CommandElementDTO createElementDTO(PhpAttribute attribute) {
        Map<String, String> values = mapping.clone().extractValues(attribute);
        return new CommandElementDTO(values.get("name"), values.get("description"), attribute);
    }

    @Override
    protected String[] getNamespaces() {
        return configuration.namespaces;
    }

    @Override
    protected String getAttributeFQCN() {
        return configuration.attributeFQCN;
    }

    public static CommandService getInstance(Project project) {
        return project.getService(CommandService.class);
    }
}
