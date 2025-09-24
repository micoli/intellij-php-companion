package org.micoli.php.configuration

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.JsonLanguage
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.yaml.YAMLLanguage
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLValue
import org.micoli.php.configuration.schema.valueGenerator.ActionIdValueGenerator
import org.micoli.php.configuration.schema.valueGenerator.CodeStyleGenerator
import org.micoli.php.configuration.schema.valueGenerator.IconValueGenerator

class SchemaCompletionContributor : CompletionContributor() {
    init {
        initializeAutocompletes()

        val generatorClass: CompletionProvider<CompletionParameters?> =
            object : CompletionProvider<CompletionParameters?>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                ) {
                    if (!ConfigurationFactory()
                        .acceptableConfigurationFiles
                        .contains(parameters.originalFile.originalFile.name)) {
                        return
                    }

                    for (action in
                        autocompleteValues[context.get(GENERATOR_CLASS) as Class<*>?]!!) {
                        result.addElement(
                            LookupElementBuilder.create(action)
                                .withPresentableText(action)
                                .withTypeText(action, true))
                    }
                }
            }

        val pattern: PatternCondition<PsiElement?> =
            object : PatternCondition<PsiElement?>("isYamlProperty") {
                override fun accepts(element: PsiElement, context: ProcessingContext): Boolean {
                    when (val parent = element.parent) {
                        is YAMLValue -> {
                            val property = parent.parent
                            if (property is YAMLKeyValue) {
                                if (acceptablePropertyName[property.keyText] != null) {
                                    context.put(
                                        GENERATOR_CLASS, acceptablePropertyName[property.keyText]!!)
                                    return true
                                }
                            }
                        }
                        is JsonStringLiteral -> {
                            val property = parent.parent
                            if (property is JsonProperty) {
                                if (acceptablePropertyName[property.name] != null) {
                                    context.put(
                                        GENERATOR_CLASS, acceptablePropertyName[property.name]!!)
                                    return true
                                }
                            }
                        }
                        else -> {
                            return false
                        }
                    }
                    return false
                }
            }
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(YAMLLanguage.INSTANCE).with(pattern),
            generatorClass,
        )

        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(JsonLanguage.INSTANCE).with(pattern),
            generatorClass,
        )
    }

    companion object {
        const val GENERATOR_CLASS: String = "generatorClass"
    }
}

private val autocompleteValues: MutableMap<Class<*>, ImmutableList<String>> = HashMap()
private val acceptablePropertyName: MutableMap<String, Class<*>> = HashMap()

private fun initializeAutocompletes() {
    if (autocompleteValues.isEmpty() && acceptablePropertyName.isEmpty()) {
        for (generator in
            listOf(ActionIdValueGenerator(), IconValueGenerator(), CodeStyleGenerator())) {
            autocompleteValues[generator::class.java] = generator.getValues()
            for (fieldName in generator.getFieldNames()) {
                acceptablePropertyName[fieldName] = generator::class.java
            }
        }
    }
}
