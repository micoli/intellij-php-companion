package org.micoli.php;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.messages.MessageBus;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.attributeNavigation.service.AttributeNavigationService;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.NoConfigurationFileException;
import org.micoli.php.events.ConfigurationEvents;
import org.micoli.php.events.IndexingEvents;
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService;
import org.micoli.php.peerNavigation.service.PeerNavigationService;
import org.micoli.php.symfony.list.RouteService;
import org.micoli.php.symfony.messenger.service.MessengerService;
import org.micoli.php.ui.Notification;

@Service(Service.Level.PROJECT)
public final class PhpCompanionProjectService implements Disposable, DumbService.DumbModeListener {

    private final Project project;
    private final @NotNull MessageBus messageBus;
    private Long configurationTimestamp = 0L;
    private final ScheduledFuture<?> scheduledTask;

    public PhpCompanionProjectService(@NotNull Project project) {
        this.project = project;
        scheduledTask = AppExecutorUtil.getAppScheduledExecutorService()
                .scheduleWithFixedDelay(this::loadConfiguration, 0, 2000, TimeUnit.MILLISECONDS);
        this.messageBus = project.getMessageBus();
        project.getMessageBus().connect().subscribe(DumbService.DUMB_MODE, this);
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

    private void loadConfiguration() {
        try {
            ConfigurationFactory.LoadedConfiguration loadedConfiguration =
                    ConfigurationFactory.loadConfiguration(project.getBasePath(), this.configurationTimestamp);
            if (loadedConfiguration == null) {
                return;
            }
            this.configurationTimestamp = loadedConfiguration.timestamp;

            MessengerService.getInstance(project)
                    .loadConfiguration(project, loadedConfiguration.configuration.symfonyMessenger);
            PeerNavigationService.getInstance(project)
                    .loadConfiguration(project, loadedConfiguration.configuration.peerNavigation);
            AttributeNavigationService.getInstance(project)
                    .loadConfiguration(project, loadedConfiguration.configuration.attributeNavigation);
            ExportSourceToMarkdownService.getInstance(project)
                    .loadConfiguration(project, loadedConfiguration.configuration.exportSourceToMarkdown);
            RouteService.getInstance(project)
                    .loadConfiguration(project, loadedConfiguration.configuration.routesConfiguration);

            messageBus
                    .syncPublisher(ConfigurationEvents.CONFIGURATION_UPDATED)
                    .configurationLoaded(loadedConfiguration.configuration);

            DaemonCodeAnalyzer.getInstance(project).restart();
            Notification.message("PHP Companion Configuration loaded");
        } catch (NoConfigurationFileException e) {
            if (!this.configurationTimestamp.equals(e.serial)) {
                Notification.error(e.getMessage());
                this.configurationTimestamp = e.serial;
            }
        } catch (ConfigurationException e) {
            if (!this.configurationTimestamp.equals(e.serial)) {
                Notification.error("Configuration error while loading:", e.getMessage());
                this.configurationTimestamp = e.serial;
            }
        }
    }

    @Override
    public void dispose() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(true);
        }
    }
}
