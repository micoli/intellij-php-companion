package org.micoli.php.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import java.util.ArrayList;
import java.util.List;

public class IgnoredPropertiesHandler extends DeserializationProblemHandler {
    private final List<String> unknownProperties = new ArrayList<>();
    private final List<String> ignoredProperties = new ArrayList<>();
    private final List<Class<?>> ignorableClasses;

    public IgnoredPropertiesHandler(List<Class<?>> ignorableClassProperties) {
        this.ignorableClasses = ignorableClassProperties;
    }

    @Override
    public boolean handleUnknownProperty(
            DeserializationContext ctxt,
            JsonParser p,
            JsonDeserializer<?> deser,
            Object beanOrClass,
            String propertyName) {

        String context = String.format(
                "%s (%s)",
                buildPropertyPath(ctxt), ctxt.getParser().currentTokenLocation().offsetDescription());
        if (ignorableClasses.contains(beanOrClass.getClass())) {
            ignoredProperties.add(context);
        } else {
            unknownProperties.add(context);
        }
        return true;
    }

    public List<String> getUnknownProperties() {
        return new ArrayList<>(unknownProperties);
    }

    public List<String> getIgnoredProperties() {
        return new ArrayList<>(ignoredProperties);
    }

    public void clearIgnoredProperties() {
        unknownProperties.clear();
        ignoredProperties.clear();
    }

    private String buildPropertyPath(DeserializationContext ctxt) {

        var pathRef = ctxt.getParser().getParsingContext();
        List<String> pathSegments = new ArrayList<>();
        var current = pathRef;
        while (current != null) {
            if (current.hasCurrentName()) {
                pathSegments.addFirst(current.getCurrentName());
            } else if (current.inArray()) {
                pathSegments.addFirst("[" + current.getCurrentIndex() + "]");
            }
            current = current.getParent();
        }

        return String.join(".", pathSegments);
    }
}
