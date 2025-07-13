package org.micoli.php.exportSourceToMarkdown.configuration;

public final class ExportSourceToMarkdownConfiguration {
    public String[] contextualNamespaces = null;
    public String template = """
            [(${#strings.isEmpty(files) ? '' : ''})]
            [# th:each="file : ${files}"]
            ## [(${file.path})]

            ```[(${file.extension})]
            [(${file.content})]
            ```

            [/]
            """;
}
