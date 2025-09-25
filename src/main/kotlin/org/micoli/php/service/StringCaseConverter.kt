package org.micoli.php.service

object StringCaseConverter {
    @JvmStatic
    fun camelToSnakeCase(camelCase: String): String {
        if (camelCase.isEmpty()) {
            return camelCase
        }

        val result = StringBuilder()
        result.append(camelCase[0].lowercaseChar())

        for (i in 1..<camelCase.length) {
            val currentChar = camelCase[i]
            if (Character.isUpperCase(currentChar)) {
                result.append('_')
                result.append(currentChar.lowercaseChar())
            } else {
                result.append(currentChar)
            }
        }

        return result.toString()
    }
}
