package org.micoli.php.configuration.schema

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.models.Configuration

class PhpCompanionJsonSchemaProviderFactory : JsonSchemaProviderFactory {
    override fun getProviders(project: Project): List<PhpCompanionJsonSchemaProvider> = listOf(PhpCompanionJsonSchemaProvider())

    class PhpCompanionJsonSchemaProvider : JsonSchemaFileProvider {
        override fun getName(): String = "PHP Companion Configuration"

        override fun isAvailable(file: VirtualFile): Boolean = ConfigurationFactory().acceptableConfigurationFiles.contains(file.name)

        override fun getSchemaType(): SchemaType = SchemaType.embeddedSchema

        override fun getSchemaVersion(): JsonSchemaVersion = JsonSchemaVersion.SCHEMA_4

        override fun getSchemaFile(): VirtualFile {
            val filename = "php-companion-schema.json"
            if (jsonSchema == null) {
                jsonSchema = ConfigurationJsonSchemaGenerator().generateSchema(Configuration::class.java)
            }
            return LightVirtualFile(filename, jsonSchema!!)
        }
    }
}

private var jsonSchema: String? = null
