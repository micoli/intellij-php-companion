package org.micoli.php.configuration.documentation.sourceCode

object SourceDocumentationGenerator {
    fun generateMarkdownDocumentation(sourcePath: String): String {
        val classInfos = KDocumentationGenerator().extractKDocs(sourcePath)
        val markdown = StringBuilder()
        for (classInfo in classInfos) {
            markdown.append("#### ").append("`").append(classInfo.name).append("`").append("\n\n")
            if (classInfo.documentation != null) {
                markdown.append(classInfo.documentation).append("\n\n")
            }

            if (!classInfo.methods.isEmpty()) {
                for (methodInfo in classInfo.methods) {
                    markdown.append("- **").append(methodInfo.name).append("**")
                    if (methodInfo.returnType != null && methodInfo.returnType != "kotlin.Unit") {
                        markdown.append(": __").append(methodInfo.returnType).append("__")
                    }
                    markdown.append("**\n")
                    if (methodInfo.documentation != null) {
                        markdown
                            .append("  ")
                            .append(methodInfo.documentation?.replace("\n".toRegex(), " ") ?: "")
                            .append("\n")
                    }

                    if (!methodInfo.parameters.isEmpty()) {
                        for (paramInfo in methodInfo.parameters) {
                            markdown
                                .append("   - `")
                                .append(paramInfo.name)
                                .append("`: ")
                                .append(paramInfo.type)
                                .append(paramInfo.documentation?.replace("\n".toRegex(), " ") ?: "")
                                .append("\n")
                        }
                        markdown.append("\n")
                    }
                }
            }
        }

        return markdown.toString()
    }
}
