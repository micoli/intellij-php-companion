package org.micoli.php.service

class HtmlStyle {
    companion object {
        fun getHtmlCss(): String {
            return """
                <style>
                    .word      { color: #999999; }
                    .symbol    { color: #999999; }
                    .keyword   { color: #ff7b72; font-weight: bold; }
                    .variable  { color: #ffa657; }
                    .comment   { color: #8b949e; }
                    .string    { color: #7ee787; }
                    .number    { color: #54aeff; font-weight: bold; }
                    .error     { color: #FF0000; }
                </style>
            """
                .trimIndent()
        }
    }
}
