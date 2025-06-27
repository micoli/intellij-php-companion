package org.micoli.php;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.concurrency.AppExecutorUtil;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.NoConfigurationFileException;
import org.micoli.php.peerNavigation.navigation.PeerNavigationGotoDeclarationHandler;
import org.micoli.php.symfony.messenger.service.MessengerServiceConfiguration;
import org.micoli.php.ui.Notification;

public class MessengerProjectComponent implements ProjectComponent {

    private final Project project;
    private Long configurationTimestamp = 0L;

    public MessengerProjectComponent(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(this::loadConfiguration, 0, 2000, TimeUnit.MILLISECONDS);
    }

    private void loadConfiguration() {
        try {
            ConfigurationFactory.LoadedConfiguration loadedConfiguration = ConfigurationFactory.loadConfiguration(project.getBasePath(), this.configurationTimestamp);
            if (loadedConfiguration == null) {
                return;
            }
            this.configurationTimestamp = loadedConfiguration.timestamp;
            MessengerServiceConfiguration.loadConfiguration(loadedConfiguration.configuration.symfonyMessenger);
            PeerNavigationGotoDeclarationHandler.loadConfiguration(loadedConfiguration.configuration.peerNavigation);
            Notification.message(getComponentName() + " Configuration loaded");
        } catch (NoConfigurationFileException e) {
            if (!this.configurationTimestamp.equals(e.serial)) {
                Notification.error(e.getMessage());
                this.configurationTimestamp = e.serial;
            }
        } catch (ConfigurationException e) {
            if (!this.configurationTimestamp.equals(e.serial)) {
                Notification.error("Configuration error while loading: " + e.getMessage());
                this.configurationTimestamp = e.serial;
            }
        }
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public @NotNull String getComponentName() {
        return "PHP Companion";
    }

    public static class MessengerStartupActivity implements StartupActivity {
        @Override
        public void runActivity(@NotNull Project project) {
            // Additional startup logic if needed
            // This runs after the project is fully loaded
        }
    }
}
