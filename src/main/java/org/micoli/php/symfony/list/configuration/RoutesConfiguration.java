package org.micoli.php.symfony.list.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import org.micoli.php.configuration.models.DisactivableConfiguration;

public final class RoutesConfiguration implements DisactivableConfiguration {
    @Override
    public boolean isDisabled() {
        return !enabled;
    }

    @Schema(description = "Enabler for panel of routes")
    public boolean enabled = true;

    @Schema(description = "List of namespaces where routes are searched")
    public String[] namespaces = {"\\App", "\\Application"};

    @Schema(description = "Attribute used to detect routes")
    public String attributeFQCN = "\\Symfony\\Component\\Routing\\Attribute\\Route";
}
