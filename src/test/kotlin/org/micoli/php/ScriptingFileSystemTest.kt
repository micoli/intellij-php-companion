package org.micoli.php

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert
import org.micoli.php.scripting.FileSystem
import org.micoli.php.utils.MyFixtureUtils

class ScriptingFileSystemTest : BasePlatformTestCase() {
    var fs: FileSystem? = null
    var vf: VirtualFile? = null

    override fun getTestDataPath(): String = "src/test/resources/filesystem/"

    fun initFixtures() {
        fs = FileSystem(project)
        vf = myFixture.copyDirectoryToProject(".", ".")
    }

    fun testItClearsPathWithValidPath() {
        initFixtures()
        val initialPathContent = MyFixtureUtils.getPathContent(vf)
        fs!!.clearPath("path0/path1", false)

        Assert.assertEquals(
            normalizeStringList(
                initialPathContent
                    .stream()
                    .filter { s: String? -> !s!!.contains("/path1") }
                    .toList()
                    .toImmutableList()),
            normalizeStringList(MyFixtureUtils.getPathContent(vf)),
        )
    }

    fun testItDoesNotClearPathIfIsNotGitIgnored() {
        initFixtures()
        val initialPathContent = MyFixtureUtils.getPathContent(vf)
        fs!!.clearPath("path0/path1")

        Assert.assertEquals(
            normalizeStringList(initialPathContent),
            normalizeStringList(MyFixtureUtils.getPathContent(vf)))
    }

    fun testItDoesClearPathIfIsNotGitIgnoredButForced() {
        initFixtures()
        val initialPathContent = MyFixtureUtils.getPathContent(vf)
        fs!!.clearPath("path0/path1", true)

        Assert.assertEquals(
            normalizeStringList(initialPathContent),
            normalizeStringList(MyFixtureUtils.getPathContent(vf)))
    }

    fun testItClearPathIfIsGitIgnoredAtRoot() {
        initFixtures()
        myFixture.addFileToProject("/.gitignore", "**/path1")

        val initialPathContent = MyFixtureUtils.getPathContent(vf)
        fs!!.clearPath("path0/path1")

        Assert.assertEquals(
            normalizeStringList(
                initialPathContent
                    .stream()
                    .filter { s: String? -> !s!!.contains("/path1") }
                    .toList()
                    .toImmutableList()),
            normalizeStringList(MyFixtureUtils.getPathContent(vf)),
        )
    }

    fun testItClearPathIfIsGitIgnoredAtSubPath() {
        initFixtures()
        myFixture.addFileToProject("/path0/.gitignore", "path1")

        val initialPathContent = MyFixtureUtils.getPathContent(vf)
        fs!!.clearPath("path0/path1")

        Assert.assertEquals(
            normalizeStringList(
                initialPathContent
                    .stream()
                    .filter { s: String? -> !s!!.contains("/path1") }
                    .toList()
                    .toImmutableList()),
            normalizeStringList(MyFixtureUtils.getPathContent(vf)),
        )
    }

    companion object {
        private fun normalizeStringList(expected: ImmutableList<String?>): String =
            expected.stream().sorted().toList().joinToString(",")
    }
}
