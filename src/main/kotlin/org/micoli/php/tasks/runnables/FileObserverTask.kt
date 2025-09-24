package org.micoli.php.tasks.runnables

import com.intellij.openapi.project.Project
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile
import org.micoli.php.ui.components.tasks.helpers.FileObserver
import org.micoli.php.ui.components.tasks.helpers.FileObserver.IconAndPrefix

class FileObserverTask(project: Project, observedFile: ObservedFile) :
    RunnableTask(project, observedFile) {
    private val fileObserver: FileObserver = FileObserver(project, observedFile)
    private val postToggle: RunnableTask?
    val taskId: String = observedFile.id!!

    var status: FileObserver.Status?
    private var firstCheck = true

    init {
        status = fileObserver.getStatus()
        postToggle =
            if (observedFile.postToggle == null) null
            else RunnableTask(project, observedFile.postToggle!!)
    }

    override fun run() {
        if (!this.fileObserver.toggle()) {
            return
        }
        postToggle?.run()
        hasChanged()
    }

    fun hasChanged(): Boolean {
        val oldStatus = status
        status = this.fileObserver.getStatus()
        if (firstCheck || status != oldStatus) {
            firstCheck = false
            return true
        }
        return false
    }

    val iconAndPrefix: IconAndPrefix
        get() = fileObserver.iconAndPrefix
}
