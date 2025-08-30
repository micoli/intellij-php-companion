package org.micoli.php.configuration.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

public class ConfigurationJsonSchemaGenerator {
    public static String generateSchema(Class<?> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
            JsonSchema jsonSchema = generator.generateSchema(clazz);

            ObjectNode schemaNode = mapper.valueToTree(jsonSchema);

            schemaNode.put("additionalProperties", false);

            return mapper.writeValueAsString(schemaNode);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du schéma", e);
        }
    }
}
