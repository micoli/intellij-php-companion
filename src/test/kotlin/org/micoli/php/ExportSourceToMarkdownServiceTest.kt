package org.micoli.php

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.*
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
        assertThat(exportedSource?.content?.trim { it <= ' ' })
            .isEqualTo(
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
            )
    }

    fun testItGeneratesMarkdownExportForSelectedFilesWithContextualNamespaces() {
        myFixture.copyDirectoryToProject("symfony-demo/src", ".")
        val filesToSelect =
            arrayOf<VirtualFile>(
                myFixture.findFileInTempDir("UseCase/ArticleViewed/Handler.php"),
                myFixture.findFileInTempDir("UseCase/ListArticles/Handler.php"),
            )
        val configuration = ExportSourceToMarkdownConfiguration()
        configuration.contextualNamespaces =
            arrayOf("App\\Entity", "App\\Repository\\PostRepository")
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
        assertThat(exportedSource?.content?.trim { it <= ' ' })
            .isEqualTo(
                """
            - /src/Entity/Post.php
            - /src/Entity/Tag.php
            - /src/Repository/PostRepository.php
            - /src/UseCase/ArticleViewed/Handler.php
            - /src/UseCase/ListArticles/Handler.php
    
            """
                    .trimIndent()
                    .trim { it <= ' ' },
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
        assertThat(exportedSource?.content?.trim { it <= ' ' })
            .isEqualTo(
                """
        - /src/path1/path1_1/path1_1_file1.txt
        - /src/path1/path1_2/path1_2_file1.txt
        - /src/path1/path1_file1.txt
        - /src/root_file1.txt

        """
                    .trimIndent()
                    .trim { it <= ' ' },
            )
    }

    fun testItCountTokens() {
        myFixture.copyDirectoryToProject("testMarkDownExporterData", ".")
        val filesToSelect = arrayOf<VirtualFile>(myFixture.findFileInTempDir("root_file1.txt"))
        val exportSourceToMarkdownService = ExportSourceToMarkdownService.getInstance(project)

        exportSourceToMarkdownService.loadConfiguration(ExportSourceToMarkdownConfiguration())
        val exportedSource = exportSourceToMarkdownService.generateMarkdownExport(filesToSelect)
        assertThat(exportedSource?.numberOfTokens).isEqualTo(18)
    }
}
