package org.micoli.php.symfony.list

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpAttribute.PhpAttributeArgument
import java.util.*
import java.util.function.Function
import org.micoli.php.service.attributes.AttributeMapping
import org.micoli.php.symfony.list.configuration.CommandsConfiguration

@Service(Service.Level.PROJECT)
class CommandService(project: Project) :
    AbstractAttributeService<CommandElementDTO, CommandsConfiguration>(project) {
    val mapping =
        AttributeMapping(
            object : LinkedHashMap<String, Function<PhpAttributeArgument, String>>() {
                init {
                    put("name") { getStringableValue(it) }
                    put("description") { getStringableValue(it) }
                    put("aliases") { getStringableValue(it) }
                }
            })

    override fun createElementDTO(
        className: String,
        attribute: PhpAttribute,
        namespace: String
    ): CommandElementDTO {
        val values = mapping.clone().extractValues(attribute)
        return CommandElementDTO(
            values["name"] ?: "", values["description"] ?: "", className, attribute)
    }

    override fun getNamespaces(): Array<String> {
        return configuration?.namespaces ?: emptyArray()
    }

    override fun getAttributeFQCN(): String? {
        return configuration?.attributeFQCN
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CommandService {
            return project.getService(CommandService::class.java)
        }
    }
}
