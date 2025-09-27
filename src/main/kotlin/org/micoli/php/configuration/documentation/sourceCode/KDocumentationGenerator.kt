package org.micoli.php.configuration.documentation.sourceCode

import io.github.classgraph.ClassGraph
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.*

class KDocumentationGenerator {

    fun extractKDocs(sourcePath: String): MutableList<ClassInfo> {
        return ClassGraph()
            .acceptPackages(sourcePath)
            .scan()
            .allClasses
            .filter { !it.name.endsWith("Error") }
            .filter { !it.name.endsWith("Exception") }
            .filter { !it.name.endsWith("\$Companion") }
            .filter { !it.isInterface }
            .sortedBy { it.name }
            .map { Class.forName(it.name).kotlin }
            .map { extractClassInfo(it) }
            .toMutableList()
    }

    fun extractClassInfo(kClass: KClass<*>): ClassInfo {
        return ClassInfo(
            name = kClass.simpleName ?: "Unknown",
            type =
                when {
                    kClass.isData -> "data class"
                    kClass.isCompanion -> "companion object"
                    kClass.objectInstance != null -> "object"
                    else -> "class"
                },
            methods =
                kClass.members
                    .filterIsInstance<KFunction<*>>()
                    .filter { it.visibility == KVisibility.PUBLIC }
                    .filter { it.name !in setOf("equals", "hashCode", "toString") }
                    .map {
                        MethodInfo(
                            name = it.name,
                            parameters =
                                it.parameters
                                    .filter { p -> p.kind == KParameter.Kind.VALUE }
                                    .map { param ->
                                        Parameter(
                                            name = param.name ?: "unknown",
                                            type = param.type.toString().replace("kotlin.", ""),
                                            defaultValue = null,
                                            documentation = null)
                                    },
                            returnType = it.returnType.toString(),
                            isConstructor = (it.name == "<init>"),
                            documentation = null)
                    },
            properties = kClass.memberProperties.map { it.name },
            documentation = null)
    }
}
