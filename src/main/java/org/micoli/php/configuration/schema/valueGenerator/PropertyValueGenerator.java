package org.micoli.php.configuration.schema.valueGenerator;

import java.util.List;

public interface PropertyValueGenerator {
    List<String> getValues();

    List<String> getFieldNames();
}
