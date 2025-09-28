package org.micoli.php.symfony.list

import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.openapi.project.Project
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpAttribute
import com.jetbrains.php.lang.psi.elements.PhpAttribute.PhpAttributeArgument
import org.micoli.php.configuration.models.DisactivableConfiguration
import org.micoli.php.service.intellij.psi.PhpUtil
import org.micoli.php.service.intellij.psi.PhpUtil.normalizeNonRootFQN

abstract class AbstractAttributeService<T, C : DisactivableConfiguration> {
    protected var project: Project
    var configuration: C? = null
        protected set

    constructor(project: Project) {
        this.project = project
    }

    open fun getElements(): MutableList<T> {
        val elements = ArrayList<T>()
        val currentConfiguration = this.configuration ?: return elements
        if (currentConfiguration.isDisabled()) {
            return elements
        }

        val phpIndex = PhpIndex.getInstance(project)
        val attributeFQCN = PhpUtil.normalizeRootFQN(this.getAttributeFQCN()!!)

        for (namespace in this.getNamespaces()) {
            val allClasses = phpIndex.getAllClassFqns(PlainPrefixMatcher(namespace))
            for (className in allClasses) {
                val normalizeNonRootFQN = normalizeNonRootFQN(className)
                val phpClass = PhpUtil.getPhpClassByFQN(project, normalizeNonRootFQN) ?: continue
                elements.addAll(
                    getElementsFromAttributes(
                        phpIndex,
                        normalizeNonRootFQN,
                        phpClass.getAttributes(attributeFQCN),
                        namespace))
                for (method in phpClass.methods) {
                    elements.addAll(
                        getElementsFromAttributes(
                            phpIndex,
                            normalizeNonRootFQN,
                            method.getAttributes(attributeFQCN),
                            namespace))
                }
            }
        }
        return elements
    }

    protected fun getElementsFromAttributes(
        phpIndex: PhpIndex,
        className: String,
        attributes: MutableCollection<PhpAttribute>,
        namespace: String
    ): MutableList<T> {
        val elements: MutableList<T> = ArrayList()
        for (attribute in attributes) {
            val element = createElementDTO(className, attribute, namespace)
            if (element != null) {
                elements.add(element)
            }
            addRelatedElements(phpIndex, elements, className, attribute, namespace)
        }
        return elements
    }

    protected open fun addRelatedElements(
        phpIndex: PhpIndex,
        elements: MutableList<T>,
        className: String,
        attribute: PhpAttribute,
        namespace: String
    ) {}

    fun loadConfiguration(configuration: C?) {
        if (configuration == null) {
            return
        }
        this.configuration = configuration
    }

    protected abstract fun createElementDTO(
        className: String,
        attribute: PhpAttribute,
        namespace: String
    ): T?

    abstract fun getNamespaces(): Array<String>

    abstract fun getAttributeFQCN(): String?

    companion object {
        protected fun getStringableValue(attributeArgument: PhpAttributeArgument): String {
            return attributeArgument.argument.value.replace("^[\"']|[\"']$".toRegex(), "")
        }
    }
}
