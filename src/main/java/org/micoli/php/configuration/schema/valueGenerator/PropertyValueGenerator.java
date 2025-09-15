package org.micoli.php.configuration.schema.valueGenerator;

import java.util.List;

public interface PropertyValueGenerator {
    enum ReferenceType {
        AS_REF,
        AS_VALUES
    }

    List<String> getValues();

    ReferenceType getType();

    String getRefId();

    List<String> getFieldNames();
}
