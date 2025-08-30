package org.micoli.php.symfony.list.configuration;

import org.micoli.php.configuration.models.DisactivableConfiguration;

public final class DoctrineEntitiesConfiguration implements DisactivableConfiguration {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean enabled = true;
    public String[] namespaces = {"\\Domain", "\\Entity"};
    public String attributeFQCN = "\\Doctrine\\ORM\\Mapping\\Table";
}
