package org.micoli.php.exportSourceToMarkdown;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.attributeNavigation.service.FileData;
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration;
import org.micoli.php.service.FileListProcessor;
import org.micoli.php.ui.Notification;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ExportSourceToMarkdownService {

    private static ExportSourceToMarkdownConfiguration configuration = new ExportSourceToMarkdownConfiguration();

    public static void loadConfiguration(Project project, ExportSourceToMarkdownConfiguration exportSourceToMarkdownConfiguration) {
        if (exportSourceToMarkdownConfiguration == null) {
            return;
        }
        configuration = exportSourceToMarkdownConfiguration;
    }

    public static ExportedSource generateMarkdownExport(Project project, VirtualFile[] selectedFiles) {
        TemplateEngine templateEngine = getTemplateEngine();

        List<VirtualFile> processedFiles = FileListProcessor.processSelectedFiles(project.getBaseDir().findChild(".aiignore"), selectedFiles);
        if (processedFiles.isEmpty()) {
            return null;
        }
        ContextualAmender contextualAmender = new ContextualAmender(project, configuration);

        Context context = new Context();

        // spotless:off
        List<VirtualFile> filesInContext = contextualAmender.amendListWithContextualFiles(
            processedFiles
        );
        context.setVariable("files", getFileData(
            project,
            sortFiles(filesInContext)
        ));
        // spotless:on

        String exportContent = templateEngine.process(configuration.template, context);

        return new ExportedSource(exportContent, getNumberOfTokens(exportContent));
    }

    private static @NotNull List<VirtualFile> sortFiles(List<VirtualFile> filesInContext) {
        List<VirtualFile> processedFiles1 = new ArrayList<>(Set.copyOf(filesInContext)).stream().sorted(Comparator.comparing(VirtualFile::getPath).thenComparing(VirtualFile::getName)).toList();
        return processedFiles1;
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
}
