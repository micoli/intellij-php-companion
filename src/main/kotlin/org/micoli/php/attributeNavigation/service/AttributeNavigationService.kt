package org.micoli.php.attributeNavigation.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import javax.script.ScriptContext
import javax.script.ScriptException
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory
import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration
import org.micoli.php.attributeNavigation.configuration.NavigationByAttributeRule
import org.micoli.php.ui.Notification

@Service(Service.Level.PROJECT)
class AttributeNavigationService(private val project: Project) {
    var rules: ImmutableList<NavigationByAttributeRule> = persistentListOf()

    fun loadConfiguration(configuration: AttributeNavigationConfiguration?) {
        if (configuration == null) {
            return
        }
        rules = persistentListOf(*configuration.rules)
    }

    fun configurationIsEmpty(): Boolean {
        return rules.isEmpty()
    }

    fun getFormattedValue(value: String?, formatterScript: String?): String? {
        if (formatterScript == null) {
            return value
        }
        val engine = GroovyScriptEngineFactory().scriptEngine
        try {
            val bindings = engine.createBindings()
            bindings["value"] = value
            engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
            return engine.eval(formatterScript, bindings) as String?
        } catch (exception: ScriptException) {
            Notification.getInstance(project).error(String.format(exception.localizedMessage))
        }
        return value
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): AttributeNavigationService {
            return project.getService(AttributeNavigationService::class.java)
        }
    }
}
