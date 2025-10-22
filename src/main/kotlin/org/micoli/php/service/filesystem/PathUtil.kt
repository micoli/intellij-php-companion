package org.micoli.php.service.filesystem

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import kotlin.Int
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

object PathUtil {
    fun getPathWithParent(containingFile: PsiFile, depth: Int): String {
        val fileName = containingFile.name
        var directory = containingFile.parent ?: return fileName

        val paths: MutableList<String?> = ArrayList()
        while (paths.size < depth) {
            paths.add(directory.name)
            directory = directory.parent ?: break
        }

        paths.reverse()
        return paths.joinToString("/") + "/" + fileName
    }

    @JvmStatic
    fun getBaseDir(project: Project): VirtualFile? {
        for (module in ModuleManager.getInstance(project).modules) {
            for (root in ModuleRootManager.getInstance(module).contentRoots) {
                return root
            }
        }
        return null
    }
}
