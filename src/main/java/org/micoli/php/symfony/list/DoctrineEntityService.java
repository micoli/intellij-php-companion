package org.micoli.php.symfony.list;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.service.StringCaseConverter;
import org.micoli.php.service.attributes.AttributeMapping;
import org.micoli.php.service.intellij.psi.PhpUtil;
import org.micoli.php.symfony.list.configuration.DoctrineEntitiesConfiguration;

public class DoctrineEntityService
        extends AbstractAttributeService<DoctrineEntityElementDTO, DoctrineEntitiesConfiguration> {
    public DoctrineEntityService() {
        mapping = new AttributeMapping(new LinkedHashMap<>() {
            {
                put("name", DoctrineEntityService::getStringableValue);
                put("schema", DoctrineEntityService::getStringableValue);
            }
        });
    }

    @Override
    protected DoctrineEntityElementDTO createElementDTO(String className, PhpAttribute attribute, String namespace) {
        Map<String, String> values = mapping.clone().extractValues(attribute);
        return new DoctrineEntityElementDTO(
                Objects.requireNonNullElse(normalizeEntityName(className, namespace), ""),
                Objects.requireNonNullElse(
                        values.get("name") != null ? values.get("name") : normalizeTableNameFromClass(className), ""),
                Objects.requireNonNullElse(values.get("schema"), ""),
                Objects.requireNonNullElse(className, ""),
                attribute);
    }

    private @NotNull String normalizeEntityName(String className, String namespace) {
        className = PhpUtil.normalizeNonRootFQN(className);
        className = className.replace(PhpUtil.normalizeNonRootFQN(namespace), "");
        return className.replaceFirst("^\\\\", "");
    }

    @Override
    protected void addRelatedElements(
            PhpIndex phpIndex,
            List<DoctrineEntityElementDTO> elements,
            String className,
            PhpAttribute attribute,
            String namespace) {
        Map<String, String> values = mapping.clone().extractValues(attribute);
        for (String childClassName : PhpUtil.getPhpClassChildByFQN(phpIndex, className)) {
            elements.add(new DoctrineEntityElementDTO(
                    Objects.requireNonNullElse(normalizeEntityName(childClassName, namespace), ""),
                    Objects.requireNonNullElse(normalizeTableNameFromClass(childClassName), ""),
                    Objects.requireNonNullElse(values.get("schema"), ""),
                    Objects.requireNonNullElse(childClassName, ""),
                    attribute));
        }
    }

    private String normalizeTableNameFromClass(String childClassName) {
        return StringCaseConverter.camelToSnakeCase(PhpUtil.getShortClassName(childClassName));
    }

    @Override
    protected String[] getNamespaces() {
        return configuration.namespaces;
    }

    public static DoctrineEntityService getInstance(Project project) {
        return project.getService(DoctrineEntityService.class);
    }

    @Override
    protected String getAttributeFQCN() {
        return configuration.attributeFQCN;
    }
}
