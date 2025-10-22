package org.micoli.php

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class PhpCompanionProjectStartup : ProjectActivity {
    override suspend fun execute(project: Project) {
        LOG.info("Starting PhpCompanionProjectService for project: ${project.name}")
        PhpCompanionProjectService.getInstance(project)
    }

    companion object {
        private val LOG = Logger.getInstance(PhpCompanionProjectStartup::class.java.getSimpleName())
    }
}
