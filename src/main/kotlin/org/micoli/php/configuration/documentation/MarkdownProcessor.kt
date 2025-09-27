package org.micoli.php.configuration.documentation

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.micoli.php.configuration.documentation.sourceCode.SourceDocumentationGenerator

class MarkdownProcessor {
    @Throws(IOException::class)
    fun processFile(filePath: String): String = processContent(Files.readString(Path.of(filePath)))

    fun processContent(content: String): String {
        val matcher: Matcher = INCLUDE_PATTERN.matcher(content)
        val result = StringBuilder()

        while (matcher.find()) {
            val replacement =
                String.format(
                    "%s\n%s\n%s",
                    matcher.group("startTag"),
                    generateDocumentation(
                        matcher.group("className"),
                        matcher.group("exportType"),
                        matcher.group("extraArgument"),
                    ),
                    matcher.group("endTag"),
                )

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement))
        }
        matcher.appendTail(result)

        return result.toString()
    }

    private fun generateDocumentation(
        className: String,
        type: String,
        extraArgument: String?
    ): String? {
        val markdownSchemaGenerator = MarkdownSchemaGenerator()
        try {
            return when (type) {
                "Example" ->
                    markdownSchemaGenerator.generateMarkdownExample(
                        Class.forName(className), extraArgument)
                "Properties" ->
                    markdownSchemaGenerator.generateMarkdownProperties(Class.forName(className))
                "Description" ->
                    markdownSchemaGenerator.generateMarkdownDescription(Class.forName(className))
                "Source" -> SourceDocumentationGenerator.generateMarkdownDocumentation(className)
                else -> throw IllegalStateException("Unexpected value: $type")
            }
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val INCLUDE_PATTERN: Pattern =
            Pattern.compile(
                "(?<startTag><!--\\s*generateDocumentation(?<exportType>(Description|Example|Properties|Source))\\(\"(?<className>[^\"]+)\",\"(?<extraArgument>[^\"]*)\"\\)\\s*-->)" +
                    "(?<oldContent>.*?)(?<endTag><!--\\s*generateDocumentationEnd\\s*-->)",
                Pattern.DOTALL,
            )
    }
}
