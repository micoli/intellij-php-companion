package org.micoli.php.configuration.schema;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import com.jetbrains.jsonSchema.extension.SchemaType;
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.micoli.php.configuration.ConfigurationFactory;

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
            String filename = "php-companion-schema.json";
            try (InputStream inputStream = getClass().getResourceAsStream("/schemas/php-companion-schema.json")) {
                if (inputStream == null) {
                    return null;
                }

                return new LightVirtualFile(filename, new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                return new LightVirtualFile(filename, "{}");
            }
        }
    }
}
