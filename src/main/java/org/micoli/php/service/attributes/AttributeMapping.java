package org.micoli.php.service.attributes;

import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import java.util.*;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class AttributeMapping implements Cloneable {
    private final List<String> positionalParamsOrder;
    private final Map<String, Function<PhpAttribute.PhpAttributeArgument, String>> valueExtractors;
    private int positionalIndex = 0;

    public AttributeMapping(
            LinkedHashMap<String, Function<PhpAttribute.PhpAttributeArgument, String>> valueExtractors) {
        this.positionalParamsOrder = extractConstructorParameters(valueExtractors);
        this.valueExtractors = valueExtractors;
    }

    @Override
    public AttributeMapping clone() {
        try {
            return (AttributeMapping) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone as failed", e);
        }
    }

    private static List<String> extractConstructorParameters(
            LinkedHashMap<String, Function<PhpAttribute.PhpAttributeArgument, String>> attributeClass) {
        if (attributeClass == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(attributeClass.keySet());
    }

    public String resolveParameterName(PhpAttribute.PhpAttributeArgument argument) {
        String name = argument.getName();
        if (name.isEmpty() && positionalIndex < positionalParamsOrder.size()) {
            name = positionalParamsOrder.get(positionalIndex++);
        }
        return name;
    }

    public Map<String, String> extractValues(@NotNull PhpAttribute attribute) {
        Map<String, String> extractedValues = new HashMap<>();

        Collection<PhpAttribute.PhpAttributeArgument> arguments = attribute.getArguments();
        for (PhpAttribute.PhpAttributeArgument argument : arguments) {
            String paramName = this.resolveParameterName(argument);
            String value = this.extractValue(paramName, argument);
            extractedValues.put(paramName, value);
        }
        return extractedValues;
    }

    public String extractValue(String paramName, PhpAttribute.PhpAttributeArgument argument) {
        Function<PhpAttribute.PhpAttributeArgument, String> callback = valueExtractors.getOrDefault(paramName, null);
        if (callback == null) {
            return null;
        }
        return callback.apply(argument);
    }
}
