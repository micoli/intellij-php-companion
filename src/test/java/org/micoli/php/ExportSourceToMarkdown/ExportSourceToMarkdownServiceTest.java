package org.micoli.php.ExportSourceToMarkdown;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.Test;
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService;

public class ExportSourceToMarkdownServiceTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/testMarkDownExporterData";
    }

    @Test
    public void testItGeneratesMarkdownExportForSelectedFiles() {
        myFixture.copyDirectoryToProject(".", ".");
        VirtualFile[] filesToSelect = { myFixture.findFileInTempDir("root_file1.txt"), myFixture.findFileInTempDir("path1"), myFixture.findFileInTempDir("path1/path1_2") };

        assertEquals("""


                ## /src/path1/path1_1/path1_1_file1.txt

                ```txt
                path1_1_file1
                ```

                ## /src/path1/path1_2/path1_2_file1.txt

                ```txt
                path1_2_file1
                ```

                ## /src/path1/path1_file1.txt

                ```txt
                path1_file1
                ```

                ## /src/root_file1.txt

                ```txt
                root_file1
                ```


                """.trim(), ExportSourceToMarkdownService.generateMarkdownExport(myFixture.getProject(), filesToSelect).trim());
    }

}
