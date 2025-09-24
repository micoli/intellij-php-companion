package org.micoli.php.openAPI

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpAttribute.PhpAttributeArgument
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import io.swagger.v3.parser.exception.EncodingNotSupportedException
import io.swagger.v3.parser.exception.ReadContentException
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream
import org.micoli.php.service.attributes.AttributeMapping
import org.micoli.php.symfony.list.AbstractAttributeService
import org.micoli.php.symfony.list.configuration.OpenAPIConfiguration

@Service(Service.Level.PROJECT)
class OpenAPIService(project: Project?) :
    AbstractAttributeService<OpenAPIPathElementDTO?, OpenAPIConfiguration?>(project) {
    init {
        mapping =
            AttributeMapping(
                object : LinkedHashMap<String, Function<PhpAttributeArgument, String>>() {
                    init {
                        put("uri") { attributeArgument: PhpAttributeArgument? ->
                            getStringableValue(attributeArgument)
                        }
                        put(
                            "description",
                        ) { attributeArgument: PhpAttributeArgument? ->
                            getStringableValue(attributeArgument)
                        }
                    }
                })
    }

    override fun createElementDTO(
        className: String?,
        attribute: PhpAttribute?,
        namespace: String?
    ): OpenAPIPathElementDTO? {
        return null
    }

    override fun getElements(): MutableList<OpenAPIPathElementDTO>? {
        if (this.configuration == null) {
            return null
        }
        val elements: MutableList<OpenAPIPathElementDTO> = ArrayList()
        for (root in this.configuration!!.specificationRoots) {
            addElementsFromOpenAPIRoot(root, elements)
        }
        return elements
    }

    private fun addElementsFromOpenAPIRoot(
        root: String?,
        elements: MutableList<OpenAPIPathElementDTO>
    ) {
        val parseOptions = ParseOptions()
        parseOptions.isResolve = true
        parseOptions.isResolveFully = true
        parseOptions.isResolveCombinators = false
        val openAPI =
            OpenAPIV3Parser().read(project.basePath + "/" + root, null, parseOptions) ?: return
        try {
            val paths = openAPI.paths
            for (pathEntry in paths) {
                val path = pathEntry.key
                val pathItem = pathEntry.value
                for (operationEntry in pathItem.readOperationsMap()) {
                    val httpMethod = operationEntry.key
                    val operationEntryValue = operationEntry.value
                    val operationId = operationEntryValue.operationId ?: continue
                    elements.add(
                        OpenAPIPathElementDTO(
                            root ?: "",
                            path ?: "",
                            httpMethod?.toString() ?: "",
                            getDescription(pathItem, operationEntryValue),
                            operationId,
                            null))
                }
            }
        } catch (_: ReadContentException) {} catch (_: EncodingNotSupportedException) {}
    }

    override fun getNamespaces(): Array<String?>? {
        return null
    }

    override fun getAttributeFQCN(): String? {
        return null
    }

    companion object {

        private fun getDescription(pathItem: PathItem, operationEntryValue: Operation): String {
            val pathDescription = pathItem.description ?: ""
            val operationDescription = operationEntryValue.description ?: ""

            return Stream.of(pathDescription, operationDescription)
                .filter { desc: String? -> !desc!!.isEmpty() }
                .collect(Collectors.joining(" "))
                .trim { it <= ' ' }
        }

        @JvmStatic
        fun getInstance(project: Project): OpenAPIService {
            return project.getService(OpenAPIService::class.java)
        }
    }
}
