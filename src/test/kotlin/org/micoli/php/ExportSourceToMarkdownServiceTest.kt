package org.micoli.php

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration

class ExportSourceToMarkdownServiceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/resources/"

    fun testItGeneratesMarkdownExportForSelectedFiles() {
        myFixture.copyDirectoryToProject("testMarkDownExporterData", ".")
        val filesToSelect =
            arrayOf<VirtualFile>(
                myFixture.findFileInTempDir("root_file1.txt"),
                myFixture.findFileInTempDir("path1"),
                myFixture.findFileInTempDir("path1/path1_2"),
            )
        val exportSourceToMarkdownService = ExportSourceToMarkdownService.getInstance(project)
        exportSourceToMarkdownService.loadConfiguration(ExportSourceToMarkdownConfiguration())
        val exportedSource = exportSourceToMarkdownService.generateMarkdownExport(filesToSelect)
        TestCase.assertEquals(
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
                .trimIndent()
                .trim { it <= ' ' },
            exportedSource?.content?.trim { it <= ' ' },
        )
    }

    fun testItGeneratesMarkdownExportForSelectedFilesWithContextualNamespaces() {
        myFixture.copyDirectoryToProject("testData/src", ".")
        val filesToSelect =
            arrayOf<VirtualFile>(
                myFixture.findFileInTempDir("Core/Query/Article/Query.php"),
                myFixture.findFileInTempDir("Core/Query/ArticleDetails"),
            )
        val configuration = ExportSourceToMarkdownConfiguration()
        configuration.contextualNamespaces = arrayOf("App\\Core\\Models", "App\\Core\\Id")
        configuration.template =
            """
        [(${'$'}{#strings.isEmpty(files) ? '' : ''})]
        [# th:each="file : ${'$'}{files}"]
        - [(${'$'}{file.path})]
        [/]

        """
                .trimIndent()
        val exportSourceToMarkdownService = ExportSourceToMarkdownService.getInstance(project)

        exportSourceToMarkdownService.loadConfiguration(configuration)
        val exportedSource = exportSourceToMarkdownService.generateMarkdownExport(filesToSelect)
        TestCase.assertEquals(
            """
        - /src/Core/Id/ArticleId.php
        - /src/Core/Models/Article.php
        - /src/Core/Models/Feed.php
        - /src/Core/Query/Article/Query.php
        - /src/Core/Query/ArticleDetails/Handler.php
        - /src/Core/Query/ArticleDetails/Query.php
        - /src/Core/Query/ArticleDetails/Result.php

        """
                .trimIndent()
                .trim { it <= ' ' },
            exportedSource?.content?.trim { it <= ' ' },
        )
    }

    fun testItGeneratesExportStringForSelectedFilesWithCustomTemplate() {
        myFixture.copyDirectoryToProject("testMarkDownExporterData", ".")
        val filesToSelect =
            arrayOf<VirtualFile>(
                myFixture.findFileInTempDir("root_file1.txt"),
                myFixture.findFileInTempDir("path1"),
                myFixture.findFileInTempDir("path1/path1_2"),
            )
        val configuration = ExportSourceToMarkdownConfiguration()
        configuration.template =
            """
        [(${'$'}{#strings.isEmpty(files) ? '' : ''})]
        [# th:each="file : ${'$'}{files}"]
        - [(${'$'}{file.path})]
        [/]

        """
                .trimIndent()

        val exportSourceToMarkdownService = ExportSourceToMarkdownService.getInstance(project)
        exportSourceToMarkdownService.loadConfiguration(configuration)
        val exportedSource = exportSourceToMarkdownService.generateMarkdownExport(filesToSelect)
        TestCase.assertEquals(
            """
        - /src/path1/path1_1/path1_1_file1.txt
        - /src/path1/path1_2/path1_2_file1.txt
        - /src/path1/path1_file1.txt
        - /src/root_file1.txt

        """
                .trimIndent()
                .trim { it <= ' ' },
            exportedSource?.content?.trim { it <= ' ' },
        )
    }

    fun testItCountTokens() {
        myFixture.copyDirectoryToProject("testMarkDownExporterData", ".")
        val filesToSelect = arrayOf<VirtualFile>(myFixture.findFileInTempDir("root_file1.txt"))
        val exportSourceToMarkdownService = ExportSourceToMarkdownService.getInstance(project)

        exportSourceToMarkdownService.loadConfiguration(ExportSourceToMarkdownConfiguration())
        val exportedSource = exportSourceToMarkdownService.generateMarkdownExport(filesToSelect)
        TestCase.assertEquals(18, exportedSource?.numberOfTokens)
    }
}
