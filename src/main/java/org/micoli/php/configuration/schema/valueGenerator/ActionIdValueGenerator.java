package org.micoli.php.configuration.schema.valueGenerator;

import com.intellij.openapi.actionSystem.ActionManager;
import java.util.List;

public class ActionIdValueGenerator implements PropertyValueGenerator {
    List<String> fieldNames = List.of("actionId");

    @Override
    public ReferenceType getType() {
        return ReferenceType.AS_REF;
    }

    @Override
    public String getRefId() {
        return "actionIds";
    }

    @Override
    public List<String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public List<String> getValues() {
        return ActionManager.getInstance().getActionIdList("").stream()
                .filter(s -> !s.contains("anonymous-group-"))
                .distinct()
                .sorted()
                .toList();
    }
}
