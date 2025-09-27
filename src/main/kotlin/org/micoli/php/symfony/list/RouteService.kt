package org.micoli.php.symfony.list

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpAttribute.PhpAttributeArgument
import java.util.*
import java.util.function.Function
import org.micoli.php.service.attributes.AttributeMapping
import org.micoli.php.symfony.list.configuration.RoutesConfiguration

@Service(Service.Level.PROJECT)
class RouteService(project: Project) :
    AbstractAttributeService<RouteElementDTO, RoutesConfiguration>(project) {
    val mapping =
        AttributeMapping(
            object : LinkedHashMap<String, Function<PhpAttributeArgument, String>>() {
                init {
                    put("path") { getStringableValue(it) }
                    put("name") { getStringableValue(it) }
                    put("requirements") { getStringableValue(it) }
                    put("options") { getStringableValue(it) }
                    put("defaults") { getStringableValue(it) }
                    put("host") { getStringableValue(it) }
                    put("methods") { extractMethods(it) }
                }
            })

    override fun createElementDTO(
        className: String,
        attribute: PhpAttribute,
        namespace: String
    ): RouteElementDTO {
        val values = mapping.clone().extractValues(attribute)
        return RouteElementDTO(
            values["path"] ?: "",
            values["name"] ?: "",
            values["methods"] ?: "",
            className,
            attribute)
    }

    override fun getNamespaces(): Array<String> {
        return configuration!!.namespaces
    }

    override fun getAttributeFQCN(): String {
        return configuration!!.attributeFQCN
    }

    companion object {
        private fun extractMethods(attributeArgument: PhpAttributeArgument): String {
            return attributeArgument.argument.value.replace("[\"'\\[\\]]".toRegex(), "")
        }

        @JvmStatic
        fun getInstance(project: Project): RouteService {
            return project.getService(RouteService::class.java)
        }
    }
}
