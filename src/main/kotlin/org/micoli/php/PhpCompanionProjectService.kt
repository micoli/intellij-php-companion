package org.micoli.php

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.DumbService.DumbModeListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.messages.MessageBus
import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.util.Map
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import org.micoli.php.attributeNavigation.service.AttributeNavigationService
import org.micoli.php.classStyles.ClassStylesService
import org.micoli.php.codeStyle.CodeStylesService
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.configuration.models.Configuration
import org.micoli.php.events.ConfigurationEvents
import org.micoli.php.events.IndexingEvents
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService
import org.micoli.php.openAPI.OpenAPIService
import org.micoli.php.peerNavigation.service.PeerNavigationService
import org.micoli.php.service.filesystem.FileListener
import org.micoli.php.service.filesystem.FileListener.VfsHandler
import org.micoli.php.service.filesystem.WatchEvent
import org.micoli.php.service.filesystem.Watchee
import org.micoli.php.symfony.list.CommandService
import org.micoli.php.symfony.list.DoctrineEntityService
import org.micoli.php.symfony.list.RouteService
import org.micoli.php.symfony.messenger.service.MessengerService
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.tasks.TasksService
import org.micoli.php.ui.Notification

@Service(Service.Level.PROJECT)
class PhpCompanionProjectService(private val project: Project) :
    Disposable, DumbModeListener, VfsHandler<String> {
    private val messageBus: MessageBus
    private var configurationTimestamp: Long = 0L
    private val scheduledTask: ScheduledFuture<*>?

    init {
        scheduledTask =
            AppExecutorUtil.getAppScheduledExecutorService()
                .scheduleWithFixedDelay({ loadConfiguration(false) }, 0, 10, TimeUnit.SECONDS)
        val fileListener = FileListener<String>(this)
        fileListener.setPatterns(
            Map.of(
                "configFile",
                Watchee(
                    listOf<PathMatcher>(
                            FileSystems.getDefault()
                                .getPathMatcher(
                                    ConfigurationFactory().acceptableConfigurationFilesGlob))
                        .toMutableList(),
                    WatchEvent.all())))
        this.messageBus = project.messageBus
        this.messageBus
            .connect()
            .subscribe<BulkFileListener>(VirtualFileManager.VFS_CHANGES, fileListener.vfsListener)
        this.messageBus.connect().subscribe<DumbModeListener>(DumbService.DUMB_MODE, this)
        loadConfiguration(true)
    }

    override fun exitDumbMode() {
        messageBus
            .syncPublisher<IndexingEvents>(IndexingEvents.INDEXING_EVENTS)
            .indexingStatusChanged(false)
    }

    override fun enteredDumbMode() {
        messageBus
            .syncPublisher<IndexingEvents>(IndexingEvents.INDEXING_EVENTS)
            .indexingStatusChanged(true)
    }

    @Synchronized
    fun loadConfiguration(force: Boolean) {
        try {
            val loadedConfiguration =
                ConfigurationFactory()
                    .loadConfiguration(project.basePath, this.configurationTimestamp, force)
            if (loadedConfiguration == null) {
                return
            }
            this.configurationTimestamp = loadedConfiguration.timestamp

            updateServicesConfigurations(loadedConfiguration.configuration)

            if (loadedConfiguration.configuration != null) {
                messageBus
                    .syncPublisher(ConfigurationEvents.CONFIGURATION_UPDATED)
                    .configurationLoaded(loadedConfiguration.configuration)
            }

            refreshHints()
            loadedConfiguration.ignoredProperties
            if (!loadedConfiguration.ignoredProperties.isEmpty()) {
                Notification.getInstance(project)
                    .message(
                        "PHP Companion Configuration loaded",
                        "Unknown properties: " +
                            loadedConfiguration.ignoredProperties.joinToString(","))
                return
            }
            Notification.getInstance(project)
                .messageWithTimeout("PHP Companion Configuration loaded", 900)
        } catch (e: NoConfigurationFileException) {
            if (this.configurationTimestamp != e.serial) {
                Notification.getInstance(project).error(e.localizedMessage)
                this.configurationTimestamp = e.serial
            }
        } catch (e: ConfigurationException) {
            if (this.configurationTimestamp != e.serial) {
                Notification.getInstance(project)
                    .error("Configuration error while loading:", e.localizedMessage)
                this.configurationTimestamp = e.serial
            }
        }
    }

    private fun refreshHints() {
        WriteCommandAction.runWriteCommandAction(project) {
            for (virtualFile in FileEditorManager.getInstance(project).openFiles) {
                virtualFile.findPsiFile(project)?.let { psiFile ->
                    DaemonCodeAnalyzer.getInstance(project).restart(psiFile)
                }
            }
        }
    }

    private fun updateServicesConfigurations(configuration: Configuration?) {
        if (configuration == null) {
            return
        }
        MessengerService.getInstance(project).loadConfiguration(configuration.symfonyMessenger)
        PeerNavigationService.getInstance(project).loadConfiguration(configuration.peerNavigation)
        AttributeNavigationService.getInstance(project)
            .loadConfiguration(configuration.attributeNavigation)
        ExportSourceToMarkdownService.getInstance(project)
            .loadConfiguration(configuration.exportSourceToMarkdown)
        RouteService.getInstance(project).loadConfiguration(configuration.routes)
        CommandService.getInstance(project).loadConfiguration(configuration.commands)
        DoctrineEntityService.getInstance(project).loadConfiguration(configuration.doctrineEntities)
        OpenAPIService.getInstance(project).loadConfiguration(configuration.openAPI)
        TasksService.getInstance(project).loadConfiguration(configuration.tasks)
        CodeStylesService.getInstance(project)
            .loadConfiguration(configuration.codeStylesSynchronization)
        SymfonyProfileService.getInstance(project).loadConfiguration(configuration.symfonyProfiler)
        ClassStylesService.getInstance(project).loadConfiguration(configuration.classStyles)
    }

    override fun dispose() {
        if (scheduledTask != null && !scheduledTask.isCancelled) {
            scheduledTask.cancel(true)
        }
    }

    override fun vfsHandle(id: String, file: VirtualFile) {
        loadConfiguration(false)
    }

    companion object {
        fun getInstance(project: Project): PhpCompanionProjectService {
            return project.getService(PhpCompanionProjectService::class.java)
        }
    }
}
