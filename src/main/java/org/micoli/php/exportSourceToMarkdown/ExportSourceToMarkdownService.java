package org.micoli.php.exportSourceToMarkdown;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
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
import org.jetbrains.annotations.Nullable;
import org.micoli.php.attributeNavigation.service.FileData;
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration;
import org.micoli.php.service.filesystem.FileListProcessor;
import org.micoli.php.ui.Notification;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

public class ExportSourceToMarkdownService {
    private final Project project;

    public ExportSourceToMarkdownService(Project project) {
        this.project = project;
    }

    public static ExportSourceToMarkdownService getInstance(Project project) {
        return project.getService(ExportSourceToMarkdownService.class);
    }

    private ExportSourceToMarkdownConfiguration configuration = new ExportSourceToMarkdownConfiguration();

    public void loadConfiguration(ExportSourceToMarkdownConfiguration exportSourceToMarkdownConfiguration) {
        if (exportSourceToMarkdownConfiguration == null) {
            return;
        }
        configuration = exportSourceToMarkdownConfiguration;
    }

    public ExportedSource generateMarkdownExport(VirtualFile[] selectedFiles) {

        List<VirtualFile> fileList = FileListProcessor.findFilesFromSelectedFiles(List.of(selectedFiles));
        if (fileList.isEmpty()) {
            return null;
        }

        ContextualAmender contextualAmender = new ContextualAmender(project, configuration);

        List<VirtualFile> filesInContext =
                getUseContextualNamespaces() ? contextualAmender.amendListWithContextualFiles(fileList) : fileList;
        VirtualFile ignoreFile = getIgnoreFile();
        List<VirtualFile> filteredFiles = FileListProcessor.filterFiles(ignoreFile, filesInContext);

        Context context = new Context();
        context.setVariable("files", getFileData(sortFiles(filteredFiles)));

        String exportContent = getTemplateEngine().process(configuration.template, context);

        return new ExportedSource(exportContent, getNumberOfTokens(exportContent));
    }

    private @Nullable VirtualFile getIgnoreFile() {
        if (getUseIgnoreFile()) {
            VirtualFile virtualFile = ProjectUtil.guessProjectDir(project);
            if (virtualFile != null) {
                return virtualFile.findChild(".aiignore");
            }
        }
        return null;
    }

    private @NotNull List<VirtualFile> sortFiles(List<VirtualFile> filesInContext) {
        return new ArrayList<>(Set.copyOf(filesInContext))
                .stream()
                        .sorted(Comparator.comparing(VirtualFile::getPath).thenComparing(VirtualFile::getName))
                        .toList();
    }

    private @NotNull List<FileData> getFileData(List<VirtualFile> processedFiles) {
        List<FileData> files = new ArrayList<>();
        String baseDir = project.getBasePath();
        assert baseDir != null;

        for (VirtualFile file : processedFiles) {
            try {
                String content = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String extension = file.getExtension() != null ? file.getExtension() : "plain";
                files.add(new FileData(file.getPath().replace(baseDir, ""), content, extension));
            } catch (IOException e) {
                Notification.getInstance(project).error(e.getMessage());
            }
        }
        return files;
    }

    private @NotNull TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("TEXT");
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }

    private int getNumberOfTokens(String exportContent) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding enc = registry.getEncoding(EncodingType.CL100K_BASE);

        return enc.countTokens(exportContent);
    }

    public void toggleUseContextualNamespaces() {
        configuration.useContextualNamespaces = !configuration.useContextualNamespaces;
    }

    public boolean getUseContextualNamespaces() {
        return configuration.useContextualNamespaces;
    }

    public boolean getUseIgnoreFile() {
        return configuration.useIgnoreFile;
    }

    public void toggleUseIgnoreFile() {
        configuration.useIgnoreFile = !configuration.useIgnoreFile;
    }
}
