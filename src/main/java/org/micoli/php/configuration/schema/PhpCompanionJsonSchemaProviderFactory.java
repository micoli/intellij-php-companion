package org.micoli.php.configuration.schema;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import com.jetbrains.jsonSchema.extension.SchemaType;
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.models.Configuration;

public class PhpCompanionJsonSchemaProviderFactory implements JsonSchemaProviderFactory {
    @NotNull @Override
    public List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
        return List.of(new PhpCompanionJsonSchemaProvider());
    }

    public static class PhpCompanionJsonSchemaProvider implements JsonSchemaFileProvider {
        @NotNull @Override
        public String getName() {
            return "PHP Companion Configuration";
        }

        @Override
        public boolean isAvailable(@NotNull VirtualFile file) {
            return ConfigurationFactory.acceptableConfigurationFiles.contains(file.getName());
        }

        @NotNull @Override
        public SchemaType getSchemaType() {
            return SchemaType.embeddedSchema;
        }

        @Override
        public JsonSchemaVersion getSchemaVersion() {
            return JsonSchemaVersion.SCHEMA_4;
        }

        @Nullable @Override
        public VirtualFile getSchemaFile() {
            return new LightVirtualFile(
                    "php-companion-schema.json",
                    new ConfigurationJsonSchemaGenerator().generateSchema(Configuration.class));
        }
    }
}
