package org.micoli.php.configuration.exceptions

import com.fasterxml.jackson.databind.exc.IgnoredPropertyException
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.InvalidNullException
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.PropertyBindingException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.databind.exc.ValueInstantiationException

object JsonExceptionMapper {
    fun getExceptionName(e: Exception): String =
      when (e) {
          is IgnoredPropertyException -> "Ignored Property"
          is InvalidDefinitionException -> "Invalid Definition"
          is InvalidFormatException -> "Invalid Format"
          is InvalidNullException -> "Invalid Null"
          is InvalidTypeIdException -> "Invalid Type Id"
          is UnrecognizedPropertyException -> "Unrecognized Property"
          is PropertyBindingException -> "Property Binding"
          is MismatchedInputException -> "Mismatched Input"
          is ValueInstantiationException -> "Value Instantiation"
          else -> throw IllegalStateException("Unexpected value: " + e.javaClass.getName())
      }
}
