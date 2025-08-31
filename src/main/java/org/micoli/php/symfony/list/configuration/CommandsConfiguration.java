package org.micoli.php.symfony.list.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import org.micoli.php.configuration.models.DisactivableConfiguration;

public final class CommandsConfiguration implements DisactivableConfiguration {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Schema(description = "Enabler for panel of console commands")
    public boolean enabled = true;

    @Schema(description = "List of namespaces where console commands are searched")
    public String[] namespaces = {"\\App", "\\Application"};

    @Schema(description = "Attribute used to detect console commands")
    public String attributeFQCN = "\\Symfony\\Component\\Console\\Attribute\\AsCommand";
}
