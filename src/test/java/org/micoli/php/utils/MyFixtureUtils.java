package org.micoli.php.utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
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

    public static List<String> filesMatching(CodeInsightTestFixture myFixture, Predicate<String> predicate) {
        return MyFixtureUtils.getPathContent(myFixture.findFileInTempDir("/")).stream()
                .filter(predicate)
                .toList();
    }

    public static List<String> filesMatchingContains(CodeInsightTestFixture myFixture, String needle) {
        return MyFixtureUtils.getPathContent(myFixture.findFileInTempDir("/")).stream()
                .filter(s -> s.contains(needle))
                .toList();
    }

    public static List<String> getPathContent(VirtualFile root) {
        MyFixtureUtils instance = new MyFixtureUtils();
        List<String> result = new ArrayList<>();
        if (root != null) {
            instance.getFilesRecursively(result, root);
        }
        return result;
    }

    private void getFilesRecursively(List<String> result, VirtualFile directory) {
        for (VirtualFile file : directory.getChildren()) {
            result.add(file.getCanonicalPath());
            if (file.isDirectory()) {
                getFilesRecursively(result, file);
            }
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

    public static void initGitRepository(@NotNull CodeInsightTestFixture myFixture) {
        myFixture.addFileToProject(
                "/.git/config",
                """
            [core]
                repositoryformatversion = 0
                filemode = true
                bare = false
                logallrefupdates = true
                ignorecase = true
                precomposeunicode = true
            """);
        myFixture.addFileToProject("/.git/description", "");
        myFixture.addFileToProject("/.git/HEAD", "ref: refs/heads/main\n");
    }
}
