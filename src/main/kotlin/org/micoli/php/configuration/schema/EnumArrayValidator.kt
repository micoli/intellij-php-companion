package org.micoli.php.configuration.schema

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumArrayValidator : ConstraintValidator<ValidEnumArray, Array<out Enum<*>>> {
    override fun isValid(value: Array<out Enum<*>>?, context: ConstraintValidatorContext): Boolean {
        return value != null
    }
}
