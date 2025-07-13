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
import java.util.List;

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

        VirtualFile[] processedFiles = FileListProcessor.processSelectedFiles(project.getBaseDir().findChild(".aiignore"), selectedFiles);
        if (processedFiles.length == 0) {
            return null;
        }

        Context context = new Context();
        context.setVariable("files", getFileData(project, processedFiles));

        String exportContent = templateEngine.process(configuration.template, context);
        return new ExportedSource(exportContent, getNumberOfTokens(exportContent));
    }

    private static @NotNull List<FileData> getFileData(Project project, VirtualFile[] processedFiles) {
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
