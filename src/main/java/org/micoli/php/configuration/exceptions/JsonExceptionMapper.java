package org.micoli.php.configuration.exceptions;

import com.fasterxml.jackson.databind.exc.*;

public class JsonExceptionMapper {
    public static String getExceptionName(Exception e) {
        return switch (e) {
            case IgnoredPropertyException ignored -> "Ignored Property";
            case InvalidDefinitionException ignored -> "Invalid Definition";
            case InvalidFormatException ignored -> "Invalid Format";
            case InvalidNullException ignored -> "Invalid Null";
            case InvalidTypeIdException ignored -> "Invalid Type Id";
            case UnrecognizedPropertyException ignored -> "Unrecognized Property";
            case PropertyBindingException ignored -> "Property Binding";
            case MismatchedInputException ignored -> "Mismatched Input";
            case ValueInstantiationException ignored -> "Value Instantiation";
            default -> throw new IllegalStateException(
                    "Unexpected value: " + e.getClass().getName());
        };
    }
}
