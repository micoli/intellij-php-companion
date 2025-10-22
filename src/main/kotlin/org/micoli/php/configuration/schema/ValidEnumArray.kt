package org.micoli.php.configuration.schema

import jakarta.validation.Constraint

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [EnumArrayValidator::class])
annotation class ValidEnumArray(val message: String = "Array values must be valid enum values")
