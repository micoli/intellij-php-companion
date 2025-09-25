package org.micoli.php.service.intellij

import com.intellij.ide.scratch.ScratchRootType
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VirtualFile

object ScratchFileUtil {
    @JvmStatic
    fun createScratchFile(
        project: Project,
        fileName: String,
        language: Language?,
        content: String
    ): VirtualFile? {
        return ApplicationManager.getApplication()
            .runWriteAction<VirtualFile?>(
                Computable {
                    try {
                        return@Computable ScratchRootType.getInstance()
                            .createScratchFile(project, fileName, language, content)
                    } catch (_: Exception) {
                        return@Computable null
                    }
                })
    }

    @JvmStatic
    fun createAndOpenScratchFile(
        project: Project,
        fileName: String,
        language: Language?,
        content: String
    ) {
        val file = createScratchFile(project, fileName, language, content)
        if (file != null) {
            FileEditorManager.getInstance(project).openFile(file, true)
        }
    }
}
