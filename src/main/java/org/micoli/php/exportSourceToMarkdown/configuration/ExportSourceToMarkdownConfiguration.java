package org.micoli.php.exportSourceToMarkdown.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

public final class ExportSourceToMarkdownConfiguration {
    public boolean useContextualNamespaces = true;
    public boolean useIgnoreFile = true;

    @Schema(
            description =
                    "List of namespaces, if an import detected in an exported classes belong to one of those namespace, than the class is added in the context",
            examples = {"App\\Core\\Models"})
    public String[] contextualNamespaces = null;

    @Schema(
            description =
                    "[Template Thymeleaf](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#standard-expression-syntax) used to generate markdown export. Accès aux variables : `files` (FileData properties `path`, `content`, et `extension`)")
    public String template =
            """
            [# th:each="file : ${files}"]
            ## [(${file.path})]

            ```[(${file.extension})]
            [(${file.content})]
            ```

            [/]
            """;
}
