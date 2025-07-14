package org.micoli.php;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class MyFixtureUtils {
    /**
     * usage : MyFixtureUtils.dumpPath(myFixture.findFileInTempDir("/"));
     */
    public static void dumpPath(VirtualFile root) {
        MyFixtureUtils instance = new MyFixtureUtils();
        if (root != null) {
            int max = instance.getMaxLength(root, 0, 0);
            System.out.println("-----");
            instance.printFilesRecursively(root, 0, max + 4);
            System.out.println("-----");
        }
    }

    private void printFilesRecursively(VirtualFile directory, int level, int max) {
        for (VirtualFile file : directory.getChildren()) {
            String filename = getFormattedFilename(level, file);
            System.out.println(filename + " ".repeat(max - filename.length()) + "|" + file.getPath());
            if (file.isDirectory()) {
                printFilesRecursively(file, level + 1, max);
            }
        }
    }

    private int getMaxLength(VirtualFile directory, int level, int max) {
        for (VirtualFile file : directory.getChildren()) {
            max = Integer.max(max, getFormattedFilename(level, file).length());
            if (file.isDirectory()) {
                max = Integer.max(max, getMaxLength(file, level + 1, max));
            }
        }
        return max;
    }

    private static @NotNull String getFormattedFilename(int level, VirtualFile file) {
        return "  ".repeat(level) + "- " + file.getName();
    }
}
