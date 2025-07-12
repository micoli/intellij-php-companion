package org.micoli.php.exportSourceToMarkdown;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.service.FileListProcessor;
import org.micoli.php.service.ScratchFileUtil;
import org.micoli.php.ui.Notification;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExportSourceToMarkdownService {
    private static final String TEMPLATE = """
            [(${#strings.isEmpty(files) ? '' : ''})]
            [# th:each="file : ${files}"]
            ## [(${file.path})]

            ```[(${file.extension})]
            [(${file.content})]
            ```

            [/]
            """;

    record FileData(String path, String content, String extension) {
    }

    public static void exportSourceToMarkdown(Project project, VirtualFile[] selectedFiles) {
        String content = generateMarkdownExport(project, selectedFiles);
        if (content == null) {
            Notification.error("No files found for export.");
            return;
        }
        ScratchFileUtil.createAndOpenScratchFile(project, "exportedSource", Language.findLanguageByID("Markdown"), content);
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
