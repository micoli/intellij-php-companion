package org.micoli.php.attributeNavigation.service;

import com.intellij.openapi.project.Project;
import java.util.ArrayList;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration;
import org.micoli.php.attributeNavigation.configuration.NavigationByAttributeRule;
import org.micoli.php.ui.Notification;

public class AttributeNavigationService {

    private static Project project;
    private static List<NavigationByAttributeRule> rules = new ArrayList<>();

    public static void loadConfiguration(Project project, AttributeNavigationConfiguration _openApiConfiguration) {
        if (_openApiConfiguration == null) {
            return;
        }
        AttributeNavigationService.project = project;
        rules = List.of(_openApiConfiguration.rules);
    }

    public static Boolean configurationIsEmpty() {
        return rules.isEmpty();
    }

    public static List<NavigationByAttributeRule> getRules() {
        return rules;
    }

    public static String getFormattedValue(String value, String formatterScript) {
        if (formatterScript == null) {
            return value;
        }
        ScriptEngine engine = new GroovyScriptEngineFactory().getScriptEngine();
        try {
            Bindings bindings = engine.createBindings();
            bindings.put("value", value);
            engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            return (String) engine.eval(formatterScript, bindings);
        } catch (ScriptException exception) {
            Notification.error(String.format(exception.getMessage()));
        }
        return value;
    }
}
