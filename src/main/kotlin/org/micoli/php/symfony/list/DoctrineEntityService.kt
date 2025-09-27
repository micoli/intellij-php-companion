package org.micoli.php.symfony.list

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpAttribute.PhpAttributeArgument
import java.util.*
import java.util.function.Function
import org.micoli.php.service.StringCaseConverter.camelToSnakeCase
import org.micoli.php.service.attributes.AttributeMapping
import org.micoli.php.service.intellij.psi.PhpUtil.getPhpClassChildByFQN
import org.micoli.php.service.intellij.psi.PhpUtil.getShortClassName
import org.micoli.php.service.intellij.psi.PhpUtil.normalizeNonRootFQN
import org.micoli.php.symfony.list.configuration.DoctrineEntitiesConfiguration

@Service(Service.Level.PROJECT)
class DoctrineEntityService(project: Project) :
    AbstractAttributeService<DoctrineEntityElementDTO, DoctrineEntitiesConfiguration>(project) {
    val mapping =
        AttributeMapping(
            object : LinkedHashMap<String, Function<PhpAttributeArgument, String>>() {
                init {
                    put("name") { getStringableValue(it) }
                    put("schema") { getStringableValue(it) }
                }
            })

    override fun createElementDTO(
        className: String,
        attribute: PhpAttribute,
        namespace: String
    ): DoctrineEntityElementDTO {
        val values: MutableMap<String, String?> = mapping.clone().extractValues(attribute)
        return DoctrineEntityElementDTO(
            normalizeEntityName(className, namespace),
            values["name"] ?: normalizeTableNameFromClass(className),
            values["schema"] ?: "",
            className,
            attribute)
    }

    private fun normalizeEntityName(className: String, namespace: String): String {
        var className = className
        className = normalizeNonRootFQN(className)
        className = className.replace(normalizeNonRootFQN(namespace), "")
        return className.replaceFirst("^\\\\".toRegex(), "")
    }

    override fun addRelatedElements(
        phpIndex: PhpIndex,
        elements: MutableList<DoctrineEntityElementDTO>,
        className: String,
        attribute: PhpAttribute,
        namespace: String
    ) {
        val values: MutableMap<String, String?> = mapping.clone().extractValues(attribute)
        for (childClassName in getPhpClassChildByFQN(phpIndex, className)) {
            elements.add(
                DoctrineEntityElementDTO(
                    normalizeEntityName(childClassName, namespace),
                    normalizeTableNameFromClass(childClassName),
                    values["schema"] ?: "",
                    childClassName,
                    attribute))
        }
    }

    private fun normalizeTableNameFromClass(childClassName: String?): String {
        return camelToSnakeCase(getShortClassName(childClassName))
    }

    override fun getNamespaces(): Array<String> {
        return configuration!!.namespaces
    }

    override fun getAttributeFQCN(): String? {
        return configuration!!.attributeFQCN
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): DoctrineEntityService {
            return project.getService(DoctrineEntityService::class.java)
        }
    }
}
