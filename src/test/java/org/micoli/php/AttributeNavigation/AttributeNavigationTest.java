package org.micoli.php.AttributeNavigation;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.micoli.php.attributeNavigation.configuration.NavigationByAttributeRule;
import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration;
import org.micoli.php.attributeNavigation.service.AttributeNavigationService;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.NoConfigurationFileException;

import java.util.Objects;

public class AttributeNavigationTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testItFormatValueUsingInlineFormatter() {
        String formattedValue = AttributeNavigationService.getFormattedValue("cde", "return ('ab-'+value+'-fg').toLowerCase()");
        assertEquals("ab-cde-fg", formattedValue);
    }

    public void testItFormatValueUsingScriptInConfiguration() {
        loadPluginConfiguration(getTestDataPath());
        NavigationByAttributeRule rule = AttributeNavigationService.getRules().getFirst();
        String formattedValue = AttributeNavigationService.getFormattedValue("'/templates/{templateId}/documents/{documentId}'", rule.formatterScript);
        assertEquals("/templates/[^/]*/documents/[^/]*:", formattedValue);
    }

    private void loadPluginConfiguration(String path) {
        AttributeNavigationConfiguration attributeNavigationConfiguration = null;
        try {
            attributeNavigationConfiguration = Objects.requireNonNull(ConfigurationFactory.loadConfiguration(path, 0L)).configuration.attributeNavigation;
        } catch (ConfigurationException | NoConfigurationFileException e) {
            throw new RuntimeException(e);
        }
        AttributeNavigationService.loadConfiguration(getProject(), attributeNavigationConfiguration);
    }
}
