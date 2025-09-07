package org.micoli.php.service.filesystem;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.*;
import java.util.*;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.jetbrains.annotations.Nullable;

public class FileListProcessor {

    public static List<VirtualFile> findFilesFromSelectedFiles(List<VirtualFile> selectedFiles) {
        Set<VirtualFile> filesSet = new LinkedHashSet<>();

        for (VirtualFile file : selectedFiles) {
            if (file == null) {
                continue;
            }
            if (file.isDirectory()) {
                listFilesRecursively(file, filesSet);
                continue;
            }
            filesSet.add(file);
        }
        return new ArrayList<>(filesSet);
    }

    public static List<VirtualFile> filterFiles(@Nullable VirtualFile ignoreFile, List<VirtualFile> filesSet) {
        IgnoreNode ignoreNode = getIgnoreNode(ignoreFile);
        if (ignoreNode == null) {
            return new ArrayList<>(filesSet);
        }

        return filesSet.stream()
                .filter(file -> ignoreNode.isIgnored(VfsUtil.getRelativePath(file, ignoreFile.getParent()), false)
                        != IgnoreNode.MatchResult.IGNORED)
                .toList();
    }

    private static @Nullable IgnoreNode getIgnoreNode(VirtualFile ignoreFile) {
        if (ignoreFile == null) {
            return null;
        }
        try {
            final IgnoreNode ignoreNode = new IgnoreNode();
            try (InputStream reader = ignoreFile.getInputStream()) {
                ignoreNode.parse(reader);
            }
            return ignoreNode;
        } catch (IOException ignored) {
            return null;
        }
    }

    private static void listFilesRecursively(VirtualFile directory, Set<VirtualFile> filesList) {
        VirtualFile[] children = directory.getChildren();

        if (children == null) {
            return;
        }
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                listFilesRecursively(child, filesList);
                continue;
            }
            filesList.add(child);
        }
    }
}
