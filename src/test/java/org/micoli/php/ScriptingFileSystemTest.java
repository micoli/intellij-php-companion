package org.micoli.php;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.List;
import org.junit.Assert;
import org.micoli.php.scripting.FileSystem;
import org.micoli.php.utils.MyFixtureUtils;

public class ScriptingFileSystemTest extends BasePlatformTestCase {
    FileSystem fs;
    VirtualFile vf;

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/filesystem/";
    }

    public void initFixtures() {
        fs = new FileSystem(getProject());
        vf = myFixture.copyDirectoryToProject(".", ".");
    }

    public void testItClearsPathWithValidPath() {
        initFixtures();
        List<String> initialPathContent = MyFixtureUtils.getPathContent(vf);
        fs.clearPath("path0/path1", false);

        Assert.assertEquals(
                normalizeStringList(initialPathContent.stream()
                        .filter(s -> !s.contains("/path1"))
                        .toList()),
                normalizeStringList(MyFixtureUtils.getPathContent(vf)));
    }

    public void testItDoesNotClearPathIfIsNotGitIgnored() {
        initFixtures();
        List<String> initialPathContent = MyFixtureUtils.getPathContent(vf);
        fs.clearPath("path0/path1");

        Assert.assertEquals(
                normalizeStringList(initialPathContent), normalizeStringList(MyFixtureUtils.getPathContent(vf)));
    }

    public void testItDoesClearPathIfIsNotGitIgnoredButForced() {
        initFixtures();
        List<String> initialPathContent = MyFixtureUtils.getPathContent(vf);
        fs.clearPath("path0/path1", true);

        Assert.assertEquals(
                normalizeStringList(initialPathContent), normalizeStringList(MyFixtureUtils.getPathContent(vf)));
    }

    public void testItClearPathIfIsGitIgnoredAtRoot() {
        initFixtures();
        myFixture.addFileToProject("/.gitignore", "**/path1");

        List<String> initialPathContent = MyFixtureUtils.getPathContent(vf);
        fs.clearPath("path0/path1");

        Assert.assertEquals(
                normalizeStringList(initialPathContent.stream()
                        .filter(s -> !s.contains("/path1"))
                        .toList()),
                normalizeStringList(MyFixtureUtils.getPathContent(vf)));
    }

    public void testItClearPathIfIsGitIgnoredAtSubPath() {
        initFixtures();
        myFixture.addFileToProject("/path0/.gitignore", "path1");

        List<String> initialPathContent = MyFixtureUtils.getPathContent(vf);
        fs.clearPath("path0/path1");

        Assert.assertEquals(
                normalizeStringList(initialPathContent.stream()
                        .filter(s -> !s.contains("/path1"))
                        .toList()),
                normalizeStringList(MyFixtureUtils.getPathContent(vf)));
    }

    private static String normalizeStringList(List<String> expected) {
        return String.join(",", expected.stream().sorted().toList());
    }
}
