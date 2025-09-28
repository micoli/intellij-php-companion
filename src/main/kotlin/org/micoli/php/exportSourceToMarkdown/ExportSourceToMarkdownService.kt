package org.micoli.php.exportSourceToMarkdown

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.EncodingType
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Set
import org.micoli.php.attributeNavigation.service.FileData
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration
import org.micoli.php.service.filesystem.FileListProcessor
import org.micoli.php.service.filesystem.FileListProcessor.findFilesFromSelectedFiles
import org.micoli.php.ui.Notification
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.StringTemplateResolver

@Service(Service.Level.PROJECT)
class ExportSourceToMarkdownService(private val project: Project) {
    private var configuration: ExportSourceToMarkdownConfiguration =
        ExportSourceToMarkdownConfiguration()

    fun loadConfiguration(
        exportSourceToMarkdownConfiguration: ExportSourceToMarkdownConfiguration?
    ) {
        if (exportSourceToMarkdownConfiguration == null) {
            return
        }
        configuration = exportSourceToMarkdownConfiguration
    }

    fun generateMarkdownExport(selectedFiles: Array<VirtualFile>): ExportedSource? {
        val fileList = findFilesFromSelectedFiles(mutableListOf(*selectedFiles))
        if (fileList.isEmpty()) {
            return null
        }

        val contextualAmender = ContextualAmender(project, configuration)

        val filesInContext =
            if (this.useContextualNamespaces)
                contextualAmender.amendListWithContextualFiles(fileList)
            else fileList
        val ignoreFile = this.ignoreFile
        val filteredFiles = FileListProcessor.filterFiles(ignoreFile, filesInContext)

        val context = Context()
        context.setVariable("files", getFileData(sortFiles(filteredFiles)))

        val exportContent = this.templateEngine.process(configuration.template, context)

        return ExportedSource(exportContent, getNumberOfTokens(exportContent))
    }

    private val ignoreFile: VirtualFile?
        get() {
            if (this.useIgnoreFile) {
                val virtualFile = project.guessProjectDir()
                if (virtualFile != null) {
                    return virtualFile.findChild(".aiignore")
                }
            }
            return null
        }

    private fun sortFiles(filesInContext: MutableList<VirtualFile>): MutableList<VirtualFile> {
        return Set.copyOf<VirtualFile>(filesInContext)
            .stream()
            .sorted(
                Comparator.comparing<VirtualFile, String> { it.path }
                    .thenComparing<String> { it.name })
            .toList()
    }

    private fun getFileData(processedFiles: MutableList<VirtualFile>): MutableList<FileData> {
        val files = ArrayList<FileData>()
        val baseDir = checkNotNull(project.basePath)
        for (file in processedFiles) {
            try {
                val content = String(file.inputStream.readAllBytes(), StandardCharsets.UTF_8)
                val extension = file.extension ?: "plain"
                files.add(FileData(file.path.replace(baseDir, ""), content, extension))
            } catch (e: IOException) {
                Notification.getInstance(project).error(e.localizedMessage)
            }
        }
        return files
    }

    private val templateEngine: TemplateEngine
        get() {
            val templateEngine = TemplateEngine()
            val resolver = StringTemplateResolver()
            resolver.setTemplateMode("TEXT")
            templateEngine.setTemplateResolver(resolver)
            return templateEngine
        }

    private fun getNumberOfTokens(exportContent: String?): Int {
        val registry = Encodings.newDefaultEncodingRegistry()
        val enc = registry.getEncoding(EncodingType.CL100K_BASE)

        return enc.countTokens(exportContent)
    }

    fun toggleUseContextualNamespaces() {
        configuration.useContextualNamespaces = !configuration.useContextualNamespaces
    }

    val useContextualNamespaces: Boolean
        get() = configuration.useContextualNamespaces

    val useIgnoreFile: Boolean
        get() = configuration.useIgnoreFile

    fun toggleUseIgnoreFile() {
        configuration.useIgnoreFile = !configuration.useIgnoreFile
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ExportSourceToMarkdownService {
            return project.getService(ExportSourceToMarkdownService::class.java)
        }
    }
}
