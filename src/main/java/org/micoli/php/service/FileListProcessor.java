package org.micoli.php.service;

import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class FileListProcessor {

    public static List<VirtualFile> processSelectedFiles(@Nullable VirtualFile ignoreFile, VirtualFile[] selectedFiles) {
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

        IgnoreNode ignoreNode = getIgnoreNode(ignoreFile);
        if (ignoreNode == null) {
            return new ArrayList<>(filesSet);
        }

        // spotless:off
        return filesSet
            .stream()
            .filter(file -> ignoreNode.isIgnored(file.getPath(), false) != IgnoreNode.MatchResult.IGNORED)
            .toList();
        // spotless:on
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
