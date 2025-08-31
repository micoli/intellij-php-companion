package org.micoli.php.symfony.list.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import org.micoli.php.configuration.models.DisactivableConfiguration;

public final class OpenAPIConfiguration implements DisactivableConfiguration {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Schema(description = "Enabler for panel of OAS routes")
    public boolean enabled = true;

    @Schema(
            description = "List of root files of swagger/openapi yaml/json files",
            examples = {"public/openapi.yaml", "private/openapi.yaml"})
    public String[] specificationRoots = {};
}
