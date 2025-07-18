package org.micoli.php.service;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathUtil {
    public static String getPathWithParent(PsiFile containingFile, int depth) {
        String fileName = containingFile.getName();
        PsiDirectory directory = containingFile.getParent();

        if (directory == null) {
            return fileName;
        }

        List<String> paths = new ArrayList<>();
        while (directory != null && paths.size() < depth) {
            paths.add(directory.getName());
            directory = directory.getParent();
        }

        Collections.reverse(paths);
        return String.join("/", paths) + "/" + fileName;
    }
}
