package org.micoli.php.service.intellij.search

import com.intellij.find.FindModel
import com.intellij.find.impl.FindInProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.usageView.UsageInfo
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

object SearchWithCompletionIndicator {
    @JvmStatic
    fun findUsagesWithProgress(
        findModel: FindModel,
        project: Project,
        maxTimeSearchWithoutResult: Int,
        callback: Consumer<MutableList<UsageInfo>>
    ) {
        ProgressManager.getInstance()
            .run(
                object : Backgroundable(project, "Finding usages", true) {
                    override fun run(indicator: ProgressIndicator) {
                        val results = Collections.synchronizedList<UsageInfo>(ArrayList())
                        val processingStarted = AtomicBoolean(false)
                        val lastUpdateTime = AtomicLong(System.currentTimeMillis())

                        FindInProjectUtil.findUsages(
                            findModel,
                            project,
                            { usageInfo: UsageInfo? ->
                                if (indicator.isCanceled) {
                                    return@findUsages false
                                }
                                processingStarted.set(true)
                                results.add(usageInfo)
                                lastUpdateTime.set(System.currentTimeMillis())
                                indicator.text = "Found ${results.size} usages"
                                return@findUsages true
                            },
                            FindInProjectUtil.setupProcessPresentation(
                                FindInProjectUtil.setupViewPresentation(findModel)))

                        while (!indicator.isCanceled && !indicator.isRunning) {
                            try {
                                Thread.sleep(100)

                                if (processingStarted.get() &&
                                    (System.currentTimeMillis() - lastUpdateTime.get()) >
                                        maxTimeSearchWithoutResult) {
                                    break
                                }
                            } catch (_: InterruptedException) {
                                Thread.currentThread().interrupt()
                                break
                            }
                        }

                        ApplicationManager.getApplication().invokeLater {
                            callback.accept(ArrayList(results))
                        }
                    }
                })
    }
}
