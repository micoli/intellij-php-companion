package org.micoli.php;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.messages.MessageBus;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.attributeNavigation.service.AttributeNavigationService;
import org.micoli.php.codeStyle.CodeStylesService;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.exceptions.NoConfigurationFileException;
import org.micoli.php.configuration.models.Configuration;
import org.micoli.php.events.ConfigurationEvents;
import org.micoli.php.events.IndexingEvents;
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService;
import org.micoli.php.openAPI.OpenAPIService;
import org.micoli.php.peerNavigation.service.PeerNavigationService;
import org.micoli.php.service.filesystem.FileListener;
import org.micoli.php.symfony.list.CommandService;
import org.micoli.php.symfony.list.DoctrineEntityService;
import org.micoli.php.symfony.list.RouteService;
import org.micoli.php.symfony.messenger.service.MessengerService;
import org.micoli.php.tasks.TasksService;
import org.micoli.php.ui.Notification;

@Service(Service.Level.PROJECT)
public final class PhpCompanionProjectService
        implements Disposable, DumbService.DumbModeListener, FileListener.VfsHandler<String> {

    private final Project project;
    private final @NotNull MessageBus messageBus;
    private Long configurationTimestamp = 0L;
    private final ScheduledFuture<?> scheduledTask;

    public PhpCompanionProjectService(@NotNull Project project) {
        this.project = project;
        scheduledTask = AppExecutorUtil.getAppScheduledExecutorService()
                .scheduleWithFixedDelay(() -> loadConfiguration(false), 0, 10, TimeUnit.SECONDS);
        FileListener<String> fileListener = new FileListener<>(this);
        fileListener.setPatterns(Map.of(
                "configFile",
                List.of(FileSystems.getDefault()
                        .getPathMatcher(new ConfigurationFactory().acceptableConfigurationFilesGlob))));
        this.messageBus = project.getMessageBus();
        this.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, fileListener.getVfsListener());
        this.messageBus.connect().subscribe(DumbService.DUMB_MODE, this);
        loadConfiguration(true);
    }

    public static PhpCompanionProjectService getInstance(@NotNull Project project) {
        return project.getService(PhpCompanionProjectService.class);
    }

    @Override
    public void exitDumbMode() {
        messageBus.syncPublisher(IndexingEvents.INDEXING_EVENTS).indexingStatusChanged(false);
    }

    @Override
    public void enteredDumbMode() {
        messageBus.syncPublisher(IndexingEvents.INDEXING_EVENTS).indexingStatusChanged(true);
    }

    public void loadConfiguration(boolean force) {
        try {
            ConfigurationFactory.LoadedConfiguration loadedConfiguration = new ConfigurationFactory()
                    .loadConfiguration(project.getBasePath(), this.configurationTimestamp, force);
            if (loadedConfiguration == null) {
                return;
            }
            this.configurationTimestamp = loadedConfiguration.getTimestamp();

            updateServicesConfigurations(loadedConfiguration.getConfiguration());

            messageBus
                    .syncPublisher(ConfigurationEvents.CONFIGURATION_UPDATED)
                    .configurationLoaded(loadedConfiguration.getConfiguration());

            DaemonCodeAnalyzer.getInstance(project).restart();
            loadedConfiguration.getIgnoredProperties();
            if (!loadedConfiguration.getIgnoredProperties().isEmpty()) {
                Notification.getInstance(project)
                        .message(
                                "PHP Companion Configuration loaded",
                                "Unknown properties: " + String.join(",", loadedConfiguration.getIgnoredProperties()));
                return;
            }
            Notification.getInstance(project).messageWithTimeout("PHP Companion Configuration loaded", 900);

        } catch (NoConfigurationFileException e) {
            if (!this.configurationTimestamp.equals(e.serial)) {
                Notification.getInstance(project).error(e.getMessage());
                this.configurationTimestamp = e.serial;
            }
        } catch (ConfigurationException e) {
            if (!this.configurationTimestamp.equals(e.serial)) {
                Notification.getInstance(project).error("Configuration error while loading:", e.getMessage());
                this.configurationTimestamp = e.serial;
            }
        }
    }

    private void updateServicesConfigurations(Configuration configuration) {
        if (configuration == null) {
            return;
        }
        MessengerService.getInstance(project).loadConfiguration(configuration.symfonyMessenger);
        PeerNavigationService.getInstance(project).loadConfiguration(configuration.peerNavigation);
        AttributeNavigationService.getInstance(project).loadConfiguration(configuration.attributeNavigation);
        ExportSourceToMarkdownService.getInstance(project).loadConfiguration(configuration.exportSourceToMarkdown);
        RouteService.getInstance(project).loadConfiguration(configuration.routesConfiguration);
        CommandService.getInstance(project).loadConfiguration(configuration.commandsConfiguration);
        DoctrineEntityService.getInstance(project).loadConfiguration(configuration.doctrineEntitiesConfiguration);
        OpenAPIService.getInstance(project).loadConfiguration(configuration.openAPIConfiguration);
        TasksService.getInstance(project).loadConfiguration(configuration.tasksConfiguration);
        CodeStylesService.getInstance(project).loadConfiguration(configuration.codeStylesSynchronization);
    }

    @Override
    public void dispose() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(true);
        }
    }

    @Override
    public void vfsHandle(String id, @NotNull VirtualFile file) {
        loadConfiguration(false);
    }
}
