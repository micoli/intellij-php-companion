package org.micoli.php.service;

import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;

public class ScratchFileUtil {

    /**
     * Creates a scratch file using ScratchRootType
     */
    public static VirtualFile createScratchFile(Project project, String fileName, Language language, String content) {
        return ApplicationManager.getApplication().runWriteAction((Computable<VirtualFile>) () -> {
            try {
                return ScratchRootType.getInstance().createScratchFile(project, fileName, language, content);
            } catch (Exception e) {
                return null;
            }
        });
    }

    /**
     * Creates a Markdown scratch file with code examples
     */
    public static VirtualFile createMarkdownScratchWithCode(Project project, String fileName) {
        Language markdown = Language.findLanguageByID("Markdown");
        if (markdown == null) {
            markdown = Language.findLanguageByID("TEXT");
        }

        String content = """
                # Code Scratch Pad

                ## Quick Code Examples

                ```java
                // Java example
                System.out.println("Hello from scratch file!");
                ```

                ```python
                # Python example
                print("Hello from scratch file!")
                ```

                ```javascript
                // JavaScript example
                console.log("Hello from scratch file!");
                ```
                """;

        return createScratchFile(project, fileName, markdown, content);
    }

    /**
     * Creates a scratch file and opens it in the editor
     */
    public static void createAndOpenScratchFile(Project project, String fileName, Language language, String content) {
        VirtualFile file = createScratchFile(project, fileName, language, content);
        if (file != null) {
            FileEditorManager.getInstance(project).openFile(file, true);
        }
    }
}
