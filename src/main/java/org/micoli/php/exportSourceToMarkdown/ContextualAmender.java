package org.micoli.php.exportSourceToMarkdown;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration;
import org.micoli.php.service.PhpUtil;

public class ContextualAmender {
    private final ExportSourceToMarkdownConfiguration configuration;
    private final @NotNull PhpIndex phpIndex;
    private final @NotNull PsiManager psiManager;

    public ContextualAmender(Project project, ExportSourceToMarkdownConfiguration configuration) {
        this.configuration = configuration;
        this.phpIndex = PhpIndex.getInstance(project);
        this.psiManager = PsiManager.getInstance(project);
    }

    public @NotNull List<VirtualFile> amendListWithContextualFiles(List<VirtualFile> processedFiles) {
        int count;
        do {
            count = processedFiles.size();
            processedFiles = innerAmendListWithContextualFiles(processedFiles);
        } while (count != processedFiles.size());
        return processedFiles;
    }

    public @NotNull List<VirtualFile> innerAmendListWithContextualFiles(List<VirtualFile> processedFiles) {
        List<VirtualFile> filesInContext = new ArrayList<>(processedFiles);
        if (configuration.contextualNamespaces == null) {
            return filesInContext;
        }
        List<VirtualFile> additionalFiles = new ArrayList<>();

        for (VirtualFile virtualFile : processedFiles) {
            PsiFile psiFile = psiManager.findFile(virtualFile);
            if (!(psiFile instanceof PhpFile phpFile)) {
                continue;
            }
            for (String fqnImport : getImports(phpFile)) {
                if (!matchContextualNamespace(fqnImport)) {
                    continue;
                }
                VirtualFile virtualFileFromFQN =
                        PhpUtil.getVirtualFileFromFQN(phpIndex, PhpUtil.normalizeRootFQN(fqnImport));
                if (virtualFileFromFQN == null) {
                    continue;
                }
                if (additionalFiles.contains(virtualFileFromFQN) || processedFiles.contains(virtualFileFromFQN)) {
                    continue;
                }
                additionalFiles.add(virtualFileFromFQN);
            }
        }
        additionalFiles.addAll(processedFiles);

        return additionalFiles;
    }

    private boolean matchContextualNamespace(String fqnImport) {
        for (String contextualNamespace : configuration.contextualNamespaces) {
            if (fqnImport.startsWith(PhpUtil.normalizeRootFQN(contextualNamespace))) {
                return true;
            }
        }
        return false;
    }

    private List<String> getImports(PhpFile phpFile) {
        List<String> imports = new ArrayList<>();

        PsiTreeUtil.processElements(phpFile, element -> {
            if (element instanceof PhpUseList useList) {
                for (PhpUse use : useList.getDeclarations()) {
                    imports.add(use.getFQN());
                }
            }
            return true;
        });
        return imports;
    }
    // public VirtualFile getVirtualFileFromFQN(Project project, String fqn) {
    // String relativePath = fqn.replace('\\', '/') + ".php";
    //
    // for (VirtualFile root : this.projectRootManager.getContentRoots()) {
    // VirtualFile file = root.findFileByRelativePath(relativePath);
    // if (file != null && file.exists()) {
    // return file;
    // }
    // }
    //
    // for (VirtualFile root : this.projectRootManager.getContentSourceRoots()) {
    // VirtualFile file = root.findFileByRelativePath(relativePath);
    // if (file != null && file.exists()) {
    // return file;
    // }
    // }
    // PhpUtil.getVirtualFileFromFQN(phpIndex, fqn).ge;
    // return null;
    // }
}
