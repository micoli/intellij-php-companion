package org.micoli.php.symfony.list;

import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.service.PhpUtil;
import org.micoli.php.service.attributes.AttributeMapping;

public abstract class AbstractAttributeService<T, C> {
    protected Project project;
    protected C configuration;
    protected AttributeMapping mapping;

    protected AbstractAttributeService() {
        this.configuration = null;
    }

    protected abstract T createElementDTO(PhpAttribute attribute);

    public C getConfiguration() {
        return configuration;
    }

    public List<T> getElements() {
        if (this.configuration == null) {
            return null;
        }

        PhpIndex phpIndex = PhpIndex.getInstance(project);
        List<T> elements = new ArrayList<>();

        for (String namespace : getNamespaces()) {
            Collection<String> allClasses = phpIndex.getAllClassFqns(new PlainPrefixMatcher(namespace));
            for (String className : allClasses) {
                PhpClass phpClass = PhpUtil.getPhpClassByFQN(project, PhpUtil.normalizeNonRootFQN(className));
                if (phpClass == null) {
                    continue;
                }
                String attributeFQCN = PhpUtil.normalizeRootFQN(getAttributeFQCN());
                elements.addAll(getElementsFromAttributes(phpClass.getAttributes(attributeFQCN)));
                for (Method method : phpClass.getMethods()) {
                    elements.addAll(getElementsFromAttributes(method.getAttributes(attributeFQCN)));
                }
            }
        }
        return elements;
    }

    protected List<T> getElementsFromAttributes(Collection<PhpAttribute> attributes) {
        List<T> elements = new ArrayList<>();
        for (PhpAttribute attribute : attributes) {
            elements.add(createElementDTO(attribute));
        }
        return elements;
    }

    protected static @NotNull String getStringableValue(PhpAttribute.PhpAttributeArgument attributeArgument) {
        return attributeArgument.getArgument().getValue().replaceAll("^[\"']|[\"']$", "");
    }

    public void loadConfiguration(Project project, C configuration) {
        this.project = project;
        this.configuration = configuration;
    }

    protected abstract String[] getNamespaces();

    protected abstract String getAttributeFQCN();
}
