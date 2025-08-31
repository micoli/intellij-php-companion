package org.micoli.php.symfony.list.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import org.micoli.php.configuration.models.DisactivableConfiguration;

public final class DoctrineEntitiesConfiguration implements DisactivableConfiguration {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Schema(description = "Enabler for panel of doctrine entities")
    public boolean enabled = true;

    @Schema(description = "List of namespaces where doctrine entities are searched")
    public String[] namespaces = {"\\Domain", "\\Entity"};

    @Schema(description = "Attribute used to detect Entities")
    public String attributeFQCN = "\\Doctrine\\ORM\\Mapping\\Table";
}
