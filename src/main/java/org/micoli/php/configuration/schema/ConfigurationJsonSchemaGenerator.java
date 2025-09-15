package org.micoli.php.configuration.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.micoli.php.configuration.schema.valueGenerator.ActionIdValueGenerator;
import org.micoli.php.configuration.schema.valueGenerator.IconValueGenerator;
import org.micoli.php.configuration.schema.valueGenerator.PropertyValueGenerator;

public class ConfigurationJsonSchemaGenerator {

    private static final String ALL_OF_PROP = "allOf";
    private static final String ITEMS_PROP = "items";
    private static final String ONE_OF_PROP = "oneOf";
    private static final String ANY_OF_PROP = "anyOf";
    private static final String PROPERTIES_PROP = "properties";
    private static final String DEFINITIONS_PATH = "definitions";
    private final Map<String, List<String>> refValues;
    private final List<PropertyValueGenerator> enumValuesGenerators;

    public ConfigurationJsonSchemaGenerator() {
        enumValuesGenerators = List.of(new ActionIdValueGenerator(), new IconValueGenerator());
        refValues = new HashMap<>();
        for (PropertyValueGenerator generator : enumValuesGenerators) {
            refValues.put(generator.getRefId(), generator.getValues());
        }
    }

    public String generateSchema(Class<?> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            ObjectNode schemaNode = mapper.valueToTree(new SchemaGenerator(
                            new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
                                    .with(new JacksonModule(
                                            JacksonOption.RESPECT_JSONPROPERTY_ORDER,
                                            JacksonOption.RESPECT_JSONPROPERTY_REQUIRED,
                                            JacksonOption.FLATTENED_ENUMS_FROM_JSONVALUE))
                                    .build())
                    .generateSchema(clazz));
            schemaNode.put("additionalProperties", false);

            modifyPropertiesWithEnumValues(schemaNode);
            addDefinitions(schemaNode, mapper);

            return mapper.writeValueAsString(schemaNode);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du schéma: " + e.getMessage(), e);
        }
    }

    private void modifyPropertiesWithEnumValues(ObjectNode schemaNode) {
        if (schemaNode.has(PROPERTIES_PROP) && schemaNode.get(PROPERTIES_PROP).isObject()) {
            ObjectNode propertiesNode = (ObjectNode) schemaNode.get(PROPERTIES_PROP);

            for (PropertyValueGenerator generator : enumValuesGenerators) {
                for (String fieldName : generator.getFieldNames()) {
                    if (!propertiesNode.has(fieldName)) {
                        continue;
                    }
                    switch (generator.getType()) {
                        case AS_REF -> {
                            ObjectNode refNode = propertiesNode.objectNode();
                            refNode.put("$ref", String.format("#/%s/%s", DEFINITIONS_PATH, generator.getRefId()));
                            propertiesNode.set(fieldName, refNode);
                        }
                        case AS_VALUES -> {
                            propertiesNode.set(
                                    fieldName,
                                    getEnumNodeValues(
                                            propertiesNode.objectNode(), refValues.get(generator.getRefId())));
                        }
                    }
                }
            }

            propertiesNode.fields().forEachRemaining(entry -> {
                if (entry.getValue().isObject()) {
                    modifyPropertiesWithEnumValues((ObjectNode) entry.getValue());
                }
            });
        }

        if (schemaNode.has(ANY_OF_PROP) && schemaNode.get(ANY_OF_PROP).isArray()) {
            ArrayNode anyOfNode = (ArrayNode) schemaNode.get(ANY_OF_PROP);
            for (int i = 0; i < anyOfNode.size(); i++) {
                JsonNode item = anyOfNode.get(i);
                if (item.isObject()) {
                    modifyPropertiesWithEnumValues((ObjectNode) item);
                }
            }
        }

        if (schemaNode.has(ONE_OF_PROP) && schemaNode.get(ONE_OF_PROP).isArray()) {
            ArrayNode oneOfNode = (ArrayNode) schemaNode.get(ONE_OF_PROP);
            for (int i = 0; i < oneOfNode.size(); i++) {
                JsonNode item = oneOfNode.get(i);
                if (item.isObject()) {
                    modifyPropertiesWithEnumValues((ObjectNode) item);
                }
            }
        }

        if (schemaNode.has(ALL_OF_PROP) && schemaNode.get(ALL_OF_PROP).isArray()) {
            ArrayNode allOfNode = (ArrayNode) schemaNode.get(ALL_OF_PROP);
            for (int i = 0; i < allOfNode.size(); i++) {
                JsonNode item = allOfNode.get(i);
                if (item.isObject()) {
                    modifyPropertiesWithEnumValues((ObjectNode) item);
                }
            }
        }

        if (schemaNode.has(ITEMS_PROP)) {
            JsonNode items = schemaNode.get(ITEMS_PROP);
            if (items.isObject()) {
                modifyPropertiesWithEnumValues((ObjectNode) items);
            } else if (items.isArray()) {
                ArrayNode itemsArray = (ArrayNode) items;
                for (int i = 0; i < itemsArray.size(); i++) {
                    JsonNode item = itemsArray.get(i);
                    if (item.isObject()) {
                        modifyPropertiesWithEnumValues((ObjectNode) item);
                    }
                }
            }
        }
    }

    private void addDefinitions(ObjectNode schemaNode, ObjectMapper mapper) {
        ObjectNode definitionsNode;
        if (schemaNode.has(DEFINITIONS_PATH)) {
            definitionsNode = (ObjectNode) schemaNode.get(DEFINITIONS_PATH);
        } else {
            definitionsNode = mapper.createObjectNode();
            schemaNode.set(DEFINITIONS_PATH, definitionsNode);
        }

        for (Map.Entry<String, List<String>> entry : refValues.entrySet()) {
            definitionsNode.set(entry.getKey(), getEnumNodeValues(mapper.createObjectNode(), entry.getValue()));
        }
    }

    private static ObjectNode getEnumNodeValues(ObjectNode actionIdsNode, List<String> entry) {
        actionIdsNode.put("type", "string");
        ArrayNode enumNode = actionIdsNode.putArray("enum");
        entry.forEach(enumNode::add);
        return actionIdsNode;
    }
}
