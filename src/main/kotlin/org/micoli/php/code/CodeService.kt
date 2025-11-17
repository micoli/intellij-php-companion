package org.micoli.php.code

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.micoli.php.code.configuration.CodeConfiguration

@Service(Service.Level.PROJECT)
class CodeService(private val project: Project) {
    fun loadConfiguration(
        codeConfiguration: CodeConfiguration?
    ) {
        if (codeConfiguration == null) {
            return
        }
        if (!codeConfiguration.enabled) {
            return
        }

    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CodeService {
            return project.getService(CodeService::class.java)
        }
    }
}
