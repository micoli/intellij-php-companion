package org.micoli.php;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.List;
import java.util.Objects;
import org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration;
import org.micoli.php.attributeNavigation.configuration.NavigationByAttributeRule;
import org.micoli.php.attributeNavigation.service.AttributeNavigationService;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.NoConfigurationFileException;

public class AttributeNavigationTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData";
    }

    public void testItFormatValueUsingInlineFormatter() {
        String formattedValue = AttributeNavigationService.getInstance(getProject())
                .getFormattedValue("cde", "return ('ab-'+value+'-fg').toLowerCase()");
        assertEquals("ab-cde-fg", formattedValue);
    }

    public void testItCanFindLineMarkersForAttributes() {
        myFixture.configureByFiles("src/UserInterface/Web/Api/Article/Get/Controller.php");
        loadPluginConfiguration(getTestDataPath());
        List<GutterMark> lineMarkers = myFixture.findAllGutters();
        assertNotEmpty(lineMarkers);

        List<GutterMark> specificMarkers = lineMarkers.stream()
                .filter(it -> {
                    String tooltipText = it.getTooltipText();
                    if (tooltipText == null) {
                        return false;
                    }
                    return tooltipText.contains("Search for [");
                })
                .toList();

        assertEquals(1, specificMarkers.size());
    }

    public void testItFormatValueUsingScriptInConfiguration() {
        AttributeNavigationService instance = loadPluginConfiguration(getTestDataPath());
        NavigationByAttributeRule rule = instance.getRules().getFirst();
        String formattedValue =
                instance.getFormattedValue("/templates/{templateId}/documents/{documentId}", rule.formatterScript);
        assertEquals("/templates/[^/]*/documents/[^/]*:", formattedValue);
    }

    private AttributeNavigationService loadPluginConfiguration(String path) {
        AttributeNavigationConfiguration attributeNavigationConfiguration = null;
        try {
            attributeNavigationConfiguration = Objects.requireNonNull(ConfigurationFactory.loadConfiguration(path, 0L))
                    .configuration
                    .attributeNavigation;
        } catch (ConfigurationException | NoConfigurationFileException e) {
            throw new RuntimeException(e);
        }
        AttributeNavigationService instance = AttributeNavigationService.getInstance(getProject());
        instance.loadConfiguration(myFixture.getProject(), attributeNavigationConfiguration);

        return instance;
    }
}
