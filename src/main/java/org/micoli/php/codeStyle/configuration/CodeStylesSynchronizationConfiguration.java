package org.micoli.php.codeStyle.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import org.micoli.php.configuration.models.DisactivableConfiguration;

public final class CodeStylesSynchronizationConfiguration implements DisactivableConfiguration {
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Schema(description = "Enabler for panel of Code style synchronization", example = "true")
    public boolean enabled = false;

    public CodeStyle[] styles = new CodeStyle[] {};
}
