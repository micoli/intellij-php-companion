package org.micoli.php.configuration.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;

public class ConfigurationJsonSchemaGenerator {

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

            return mapper.writeValueAsString(schemaNode);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du schéma: " + e.getMessage(), e);
        }
    }
}
