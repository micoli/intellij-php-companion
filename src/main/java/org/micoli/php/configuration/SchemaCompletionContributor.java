package org.micoli.php.configuration;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.json.JsonLanguage;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;
import org.micoli.php.configuration.schema.valueGenerator.ActionIdValueGenerator;
import org.micoli.php.configuration.schema.valueGenerator.CodeStyleGenerator;
import org.micoli.php.configuration.schema.valueGenerator.IconValueGenerator;
import org.micoli.php.configuration.schema.valueGenerator.PropertyValueGenerator;

public class SchemaCompletionContributor extends CompletionContributor {
    private static final Map<Class<?>, List<String>> autocompleteValues = new HashMap<>();
    private static final Map<String, Class<?>> acceptablePropertyName = new HashMap<>();
    public static final String GENERATOR_CLASS = "generatorClass";

    public SchemaCompletionContributor() {
        initializeAutocompletes();

        CompletionProvider<CompletionParameters> generatorClass = new CompletionProvider<>() {
            @Override
            protected void addCompletions(
                    @NotNull CompletionParameters parameters,
                    @NotNull ProcessingContext context,
                    @NotNull CompletionResultSet result) {
                if (!ConfigurationFactory.acceptableConfigurationFiles.contains(
                        parameters.getOriginalFile().getOriginalFile().getName())) {
                    return;
                }

                for (String action : autocompleteValues.get((Class<?>) context.get(GENERATOR_CLASS))) {
                    result.addElement(LookupElementBuilder.create(action)
                            .withPresentableText(action)
                            .withTypeText(action, true));
                }
            }
        };

        PatternCondition<PsiElement> pattern = new PatternCondition<>("isYamlProperty") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                switch (element.getParent()) {
                    case YAMLValue parent -> {
                        if (parent.getParent() instanceof YAMLKeyValue property) {
                            if (acceptablePropertyName.get(property.getKeyText()) != null) {
                                context.put(GENERATOR_CLASS, acceptablePropertyName.get(property.getKeyText()));
                                return true;
                            }
                        }
                    }
                    case JsonStringLiteral parent -> {
                        if (parent.getParent() instanceof JsonProperty property) {
                            if (acceptablePropertyName.get(property.getName()) != null) {
                                context.put(GENERATOR_CLASS, acceptablePropertyName.get(property.getName()));
                                return true;
                            }
                        }
                    }
                    default -> {
                        return false;
                    }
                }
                return false;
            }
        };
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .withLanguage(YAMLLanguage.INSTANCE)
                        .with(pattern),
                generatorClass);

        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .withLanguage(JsonLanguage.INSTANCE)
                        .with(pattern),
                generatorClass);
    }

    private static void initializeAutocompletes() {
        if (autocompleteValues.isEmpty() && acceptablePropertyName.isEmpty()) {
            for (PropertyValueGenerator generator :
                    List.of(new ActionIdValueGenerator(), new IconValueGenerator(), new CodeStyleGenerator())) {
                autocompleteValues.put(generator.getClass(), generator.getValues());
                for (String fieldName : generator.getFieldNames()) {
                    acceptablePropertyName.put(fieldName, generator.getClass());
                }
            }
        }
    }
}
