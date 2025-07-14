package org.micoli.php.exportSourceToMarkdown;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.attributeNavigation.service.FileData;
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration;
import org.micoli.php.service.FileListProcessor;
import org.micoli.php.ui.Notification;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

public class ExportSourceToMarkdownService {

    private static ExportSourceToMarkdownConfiguration configuration = new ExportSourceToMarkdownConfiguration();

    public static void loadConfiguration(
            Project project, ExportSourceToMarkdownConfiguration exportSourceToMarkdownConfiguration) {
        if (exportSourceToMarkdownConfiguration == null) {
            return;
        }
        configuration = exportSourceToMarkdownConfiguration;
    }

    public static ExportedSource generateMarkdownExport(Project project, VirtualFile[] selectedFiles) {

        List<VirtualFile> fileList = FileListProcessor.findFilesFromSelectedFiles(List.of(selectedFiles));
        if (fileList.isEmpty()) {
            return null;
        }

        ContextualAmender contextualAmender = new ContextualAmender(project, configuration);

        List<VirtualFile> filesInContext =
                getUseContextualNamespaces() ? contextualAmender.amendListWithContextualFiles(fileList) : fileList;
        List<VirtualFile> filteredFiles = FileListProcessor.filterFiles(
                getUseIgnoreFile() ? project.getBaseDir().findChild(".aiignore") : null,
                project.getBaseDir(),
                filesInContext);

        Context context = new Context();
        context.setVariable("files", getFileData(project, sortFiles(filteredFiles)));

        String exportContent = getTemplateEngine().process(configuration.template, context);

        return new ExportedSource(exportContent, getNumberOfTokens(exportContent));
    }

    private static @NotNull List<VirtualFile> sortFiles(List<VirtualFile> filesInContext) {
        return new ArrayList<>(Set.copyOf(filesInContext))
                .stream()
                        .sorted(Comparator.comparing(VirtualFile::getPath).thenComparing(VirtualFile::getName))
                        .toList();
    }

    private static @NotNull List<FileData> getFileData(Project project, List<VirtualFile> processedFiles) {
        List<FileData> files = new ArrayList<>();
        String baseDir = project.getBasePath();
        assert baseDir != null;

        for (VirtualFile file : processedFiles) {
            try {
                String content = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String extension = file.getExtension() != null ? file.getExtension() : "plain";
                files.add(new FileData(file.getPath().replace(baseDir, ""), content, extension));
            } catch (IOException e) {
                Notification.error(e.getMessage());
            }
        }
        return files;
    }

    private static @NotNull TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("TEXT");
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }

    private static int getNumberOfTokens(String exportContent) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding enc = registry.getEncoding(EncodingType.CL100K_BASE);

        return enc.countTokens(exportContent);
    }

    public static void toggleUseContextualNamespaces() {
        configuration.useContextualNamespaces = !configuration.useContextualNamespaces;
    }

    public static boolean getUseContextualNamespaces() {
        return configuration.useContextualNamespaces;
    }

    public static boolean getUseIgnoreFile() {
        return configuration.useIgnoreFile;
    }

    public static void toggleUseIgnoreFile() {
        configuration.useIgnoreFile = !configuration.useIgnoreFile;
    }
}
