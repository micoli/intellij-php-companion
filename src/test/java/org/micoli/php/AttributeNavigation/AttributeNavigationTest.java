package org.micoli.php.AttributeNavigation;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.micoli.php.attributeNavigation.configuration.NavigationByAttributeRule;
import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration;
import org.micoli.php.attributeNavigation.service.AttributeNavigationService;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.NoConfigurationFileException;

import java.util.List;
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

    public void testItCanFindLineMarkersForAttributes() {
        myFixture.configureByFiles("src/UserInterface/Web/Api/Article/Get/Controller.php");
        loadPluginConfiguration(getTestDataPath());
        List<GutterMark> lineMarkers = myFixture.findAllGutters();
        assertNotEmpty(lineMarkers);

        List<GutterMark> specificMarkers = lineMarkers.stream().filter(it -> {
            String tooltipText = it.getTooltipText();
            if (tooltipText == null) {
                return false;
            }
            return tooltipText.contains("Search for [");
        }).toList();

        assertEquals(1, specificMarkers.size());
    }

    public void testItFormatValueUsingScriptInConfiguration() {
        loadPluginConfiguration(getTestDataPath());
        NavigationByAttributeRule rule = AttributeNavigationService.getRules().getFirst();
        String formattedValue = AttributeNavigationService.getFormattedValue("/templates/{templateId}/documents/{documentId}", rule.formatterScript);
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
