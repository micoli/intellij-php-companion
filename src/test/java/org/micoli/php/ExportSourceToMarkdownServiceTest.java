package org.micoli.php;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService;
import org.micoli.php.exportSourceToMarkdown.ExportedSource;
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration;

public class ExportSourceToMarkdownServiceTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/resources/";
    }

    public void testItGeneratesMarkdownExportForSelectedFiles() {
        myFixture.copyDirectoryToProject("testMarkDownExporterData/.", ".");
        VirtualFile[] filesToSelect = {
            myFixture.findFileInTempDir("root_file1.txt"),
            myFixture.findFileInTempDir("path1"),
            myFixture.findFileInTempDir("path1/path1_2")
        };
        ExportSourceToMarkdownService exportSourceToMarkdownService =
                ExportSourceToMarkdownService.getInstance(getProject());
        exportSourceToMarkdownService.loadConfiguration(
                myFixture.getProject(), new ExportSourceToMarkdownConfiguration());
        ExportedSource exportedSource = exportSourceToMarkdownService.generateMarkdownExport(filesToSelect);
        assertEquals(
                """
                ## /src/path1/path1_1/path1_1_file1.txt

                ```txt
                path1_1_file1
                ```

                ## /src/path1/path1_2/path1_2_file1.txt

                ```txt
                path1_2_file1
                ```

                ## /src/path1/path1_file1.txt

                ```txt
                path1_file1
                ```

                ## /src/root_file1.txt

                ```txt
                root_file1
                ```
                """
                        .trim(),
                exportedSource.content().trim());
    }

    public void testItGeneratesMarkdownExportForSelectedFilesWithContextualNamespaces() {
        myFixture.copyDirectoryToProject("testData/src", ".");
        VirtualFile[] filesToSelect = {
            myFixture.findFileInTempDir("Core/Query/Article/Query.php"),
            myFixture.findFileInTempDir("Core/Query/ArticleDetails")
        };
        ExportSourceToMarkdownConfiguration configuration = new ExportSourceToMarkdownConfiguration();
        configuration.contextualNamespaces = new String[] {"App\\Core\\Models", "App\\Core\\Id"};
        configuration.template =
                """
                [(${#strings.isEmpty(files) ? '' : ''})]
                [# th:each="file : ${files}"]
                - [(${file.path})]
                [/]
                """;
        ExportSourceToMarkdownService exportSourceToMarkdownService =
                ExportSourceToMarkdownService.getInstance(getProject());

        exportSourceToMarkdownService.loadConfiguration(myFixture.getProject(), configuration);
        ExportedSource exportedSource = exportSourceToMarkdownService.generateMarkdownExport(filesToSelect);
        assertEquals(
                """
                - /src/Core/Id/ArticleId.php
                - /src/Core/Models/Article.php
                - /src/Core/Models/Feed.php
                - /src/Core/Query/Article/Query.php
                - /src/Core/Query/ArticleDetails/Handler.php
                - /src/Core/Query/ArticleDetails/Query.php
                - /src/Core/Query/ArticleDetails/Result.php
                """
                        .trim(),
                exportedSource.content().trim());
    }

    public void testItGeneratesExportStringForSelectedFilesWithCustomTemplate() {
        myFixture.copyDirectoryToProject("testMarkDownExporterData/.", ".");
        VirtualFile[] filesToSelect = {
            myFixture.findFileInTempDir("root_file1.txt"),
            myFixture.findFileInTempDir("path1"),
            myFixture.findFileInTempDir("path1/path1_2")
        };
        ExportSourceToMarkdownConfiguration configuration = new ExportSourceToMarkdownConfiguration();
        configuration.template =
                """
                [(${#strings.isEmpty(files) ? '' : ''})]
                [# th:each="file : ${files}"]
                - [(${file.path})]
                [/]
                """;

        ExportSourceToMarkdownService exportSourceToMarkdownService =
                ExportSourceToMarkdownService.getInstance(getProject());
        exportSourceToMarkdownService.loadConfiguration(myFixture.getProject(), configuration);
        ExportedSource exportedSource = exportSourceToMarkdownService.generateMarkdownExport(filesToSelect);
        assertEquals(
                """
                - /src/path1/path1_1/path1_1_file1.txt
                - /src/path1/path1_2/path1_2_file1.txt
                - /src/path1/path1_file1.txt
                - /src/root_file1.txt
                """
                        .trim(),
                exportedSource.content().trim());
    }

    public void testItCountTokens() {
        myFixture.copyDirectoryToProject("testMarkDownExporterData/.", ".");
        VirtualFile[] filesToSelect = {myFixture.findFileInTempDir("root_file1.txt")};
        ExportSourceToMarkdownService exportSourceToMarkdownService =
                ExportSourceToMarkdownService.getInstance(getProject());

        exportSourceToMarkdownService.loadConfiguration(
                myFixture.getProject(), new ExportSourceToMarkdownConfiguration());
        ExportedSource exportedSource = exportSourceToMarkdownService.generateMarkdownExport(filesToSelect);
        assertEquals(18, exportedSource.numberOfTokens());
    }
}
