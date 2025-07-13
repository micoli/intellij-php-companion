package org.micoli.php.exportSourceToMarkdown;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
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

    public static String generateMarkdownExport(Project project, VirtualFile[] selectedFiles) {
        TemplateEngine templateEngine = getTemplateEngine();

        VirtualFile[] processedFiles = FileListProcessor.processSelectedFiles(project.getBaseDir().findChild(".aiignore"), selectedFiles);
        if (processedFiles.length == 0) {
            return null;
        }
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

        Context context = new Context();
        context.setVariable("files", files);

        return templateEngine.process(TEMPLATE, context);
    }

    private static @NotNull TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("TEXT");
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }
}
