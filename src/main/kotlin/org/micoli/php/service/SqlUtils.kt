package org.micoli.php.service

class SqlUtils {
    object Companion {
        fun formatHtmlSql(sql: String): String {
            return formatSql(sql).replace("\n", "\n<br/>")
        }

        fun formatSql(sql: String): String {
            return sql.replace(Regex("\\s+"), " ")
                .replace(Regex("\\bFROM\\b", RegexOption.IGNORE_CASE), "\nFROM")
                .replace(Regex("\\bINNER\\s+JOIN\\b", RegexOption.IGNORE_CASE), "\nINNER JOIN")
                .replace(Regex("\\bLEFT\\s+JOIN\\b", RegexOption.IGNORE_CASE), "\nLEFT JOIN")
                .replace(Regex("\\bRIGHT\\s+JOIN\\b", RegexOption.IGNORE_CASE), "\nRIGHT JOIN")
                .replace(Regex("\\bWHERE\\b", RegexOption.IGNORE_CASE), "\nWHERE")
                .replace(Regex("\\bGROUP\\s+BY\\b", RegexOption.IGNORE_CASE), "\nGROUP BY")
                .replace(Regex("\\bORDER\\s+BY\\b", RegexOption.IGNORE_CASE), "\nORDER BY")
                .replace(Regex("\\bHAVING\\b", RegexOption.IGNORE_CASE), "\nHAVING")
                .replace(Regex("\\bLIMIT\\b", RegexOption.IGNORE_CASE), "\nLIMIT")
                .trim()
        }
    }
}
