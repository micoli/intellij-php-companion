package org.micoli.php.utils

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import java.util.function.Predicate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class MyFixtureUtils {
    private fun getFilesRecursively(result: MutableList<String?>, directory: VirtualFile) {
        for (file in directory.children) {
            result.add(file.canonicalPath)
            if (file.isDirectory) {
                getFilesRecursively(result, file)
            }
        }
    }

    private fun printFilesRecursively(directory: VirtualFile, level: Int, max: Int) {
        for (file in directory.children) {
            val filename: String = getFormattedFilename(level, file)
            println(filename + " ".repeat(max - filename.length) + "|" + file.path)
            if (file.isDirectory) {
                printFilesRecursively(file, level + 1, max)
            }
        }
    }

    private fun getMaxLength(directory: VirtualFile, level: Int, max: Int): Int {
        var max = max
        for (file in directory.children) {
            max = Integer.max(max, getFormattedFilename(level, file).length)
            if (file.isDirectory) {
                max = Integer.max(max, getMaxLength(file, level + 1, max))
            }
        }
        return max
    }

    companion object {
        /** usage : MyFixtureUtils.dumpPath(myFixture.findFileInTempDir("/")); */
        fun dumpPath(root: VirtualFile?) {
            val instance = MyFixtureUtils()
            if (root != null) {
                val max = instance.getMaxLength(root, 0, 0)
                println("-----")
                instance.printFilesRecursively(root, 0, max + 4)
                println("-----")
            }
        }

        fun filesMatching(myFixture: CodeInsightTestFixture, predicate: Predicate<String?>?): MutableList<String?> {
            return getPathContent(myFixture.findFileInTempDir("/")).stream().filter(predicate).toList()
        }

        fun filesMatchingContains(myFixture: CodeInsightTestFixture, needle: String): MutableList<String?> {
            return getPathContent(myFixture.findFileInTempDir("/")).stream().filter { s: String? -> s!!.contains(needle) }.toList()
        }

        fun getPathContent(root: VirtualFile?): ImmutableList<String?> {
            val instance = MyFixtureUtils()
            val result: MutableList<String?> = ArrayList()
            if (root != null) {
                instance.getFilesRecursively(result, root)
            }
            return result.toImmutableList()
        }

        private fun getFormattedFilename(level: Int, file: VirtualFile): String {
            return "  ".repeat(level) + "- " + file.name
        }

        fun initGitRepository(myFixture: CodeInsightTestFixture) {
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

                """
                .trimIndent(),
            )
            myFixture.addFileToProject("/.git/description", "")
            myFixture.addFileToProject("/.git/HEAD", "ref: refs/heads/main\n")
        }
    }
}
