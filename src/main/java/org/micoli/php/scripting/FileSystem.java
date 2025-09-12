package org.micoli.php.scripting;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.ui.Notification;

public final class FileSystem {
    private static final Logger LOG = Logger.getInstance(FileSystem.class.getSimpleName());
    private final Project project;
    private final LocalFileSystem localFileSystem;

    public FileSystem(Project project) {
        this.project = project;
        this.localFileSystem = LocalFileSystem.getInstance();
    }

    public void clearPath(String path) {
        internalClearPath(path, true);
    }

    public void clearPath(String path, boolean mustBeGitIgnored) {
        internalClearPath(path, mustBeGitIgnored);
    }

    private void internalClearPath(String path, boolean mustBeGitIgnored) {
        try {

            VirtualFile virtualBaseDir = getBaseDir();
            if (virtualBaseDir == null) {
                throw new ScriptingError("No base directory found.");
            }

            VirtualFile virtualPath = getVirtualPath(virtualBaseDir, path);

            if (virtualPath == null || virtualPath.getCanonicalPath() == null) {
                return;
            }

            if (virtualBaseDir.getCanonicalPath() == null) {
                return;
            }

            if (!virtualPath.getCanonicalPath().startsWith(virtualBaseDir.getCanonicalPath())) {
                return;
            }

            if (mustBeGitIgnored && !isIgnored(virtualBaseDir, virtualPath)) {
                throw new ScriptingError("The path is not ignored by git: " + path);
            }

            VirtualFile virtualParent = virtualPath.getParent();
            String parentPath = virtualParent.getCanonicalPath();

            WriteAction.run(() -> {
                virtualPath.delete(this);
            });

            if (parentPath != null) {
                this.localFileSystem.refreshAndFindFileByIoFile(new File(parentPath));
            }

        } catch (ScriptingError e) {
            Notification.error(e.getMessage());
        } catch (Exception e) {
            LOG.warn("Error while cleaning path: " + path + " " + e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    private VirtualFile getBaseDir() {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
                return root;
            }
        }
        return null;
    }

    private @Nullable VirtualFile getVirtualPath(@NotNull VirtualFile virtualBaseDir, String path) {
        VirtualFile virtualPath = virtualBaseDir.findFileByRelativePath(path);
        if (virtualPath == null) {
            return null;
        }
        if (!virtualPath.exists()) {
            return null;
        }
        return virtualPath;
    }

    private boolean isIgnored(VirtualFile virtualRoot, VirtualFile virtualFile) {
        VirtualFile currentPath = virtualFile.getParent();
        while (currentPath != null && !currentPath.equals(virtualRoot.getParent())) {
            VirtualFile ignoreFile = currentPath.findChild(".gitignore");
            if (ignoreFile == null) {
                currentPath = currentPath.getParent();
                continue;
            }
            IgnoreNode ignoreNode = new IgnoreNode();
            try (InputStream reader = ignoreFile.getInputStream()) {
                ignoreNode.parse(reader);
                IgnoreNode.MatchResult ignored = ignoreNode.isIgnored(virtualFile.getName(), currentPath.isDirectory());
                if (ignored == IgnoreNode.MatchResult.IGNORED) {
                    return true;
                }
            } catch (IOException ignored) {
                return false;
            }
            currentPath = currentPath.getParent();
        }
        return false;
    }
}
