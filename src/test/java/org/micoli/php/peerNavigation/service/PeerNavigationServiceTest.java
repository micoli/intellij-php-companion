package org.micoli.php.peerNavigation.service;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.NoConfigurationFileException;
import org.micoli.php.peerNavigation.configuration.PeerNavigationConfiguration;
import org.micoli.php.service.PhpUtil;

import java.util.List;
import java.util.Objects;

public class PeerNavigationServiceTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testItFindsPeerElement() {
        myFixture.copyDirectoryToProject("src", "src");
        myFixture.copyDirectoryToProject("tests", "tests");
        loadPluginConfiguration(getTestDataPath());
        PhpClass fqn1 = PhpUtil.getPhpClassByFQN(getProject(), "\\App\\UserInterface\\Web\\Api\\Article\\Get\\Controller");
        PhpClass fqn2 = PhpUtil.getPhpClassByFQN(getProject(), "\\App\\Tests\\Func\\UserInterface\\Web\\Api\\Article\\Get\\ControllerTest");
        assertNotNull(fqn1);
        assertNotNull(fqn2);
        assertEquals(fqn2, PeerNavigationService.getPeersElement(fqn1).getFirst());
        assertEquals(fqn1, PeerNavigationService.getPeersElement(fqn2).getFirst());
    }

    public void testItCanFindLineMarkersFor() {
        myFixture.copyDirectoryToProject("src", "src");
        myFixture.copyDirectoryToProject("tests", "tests");
        myFixture.configureByFiles("src/UserInterface/Web/Api/Article/Get/Controller.php");
        loadPluginConfiguration(getTestDataPath());
        List<GutterMark> lineMarkers = myFixture.findAllGutters();
        assertNotEmpty(lineMarkers);

        List<GutterMark> specificMarkers = lineMarkers.stream().filter(it -> {
            String tooltipText = it.getTooltipText();
            if (tooltipText == null) {
                return false;
            }
            return tooltipText.contains("Navigate to [");
        }).toList();

        assertEquals(1, specificMarkers.size());
    }

    private void loadPluginConfiguration(String path) {
        PeerNavigationConfiguration peerNavigationConfiguration = null;
        try {
            peerNavigationConfiguration = Objects.requireNonNull(ConfigurationFactory.loadConfiguration(path, 0L)).configuration.peerNavigation;
        } catch (ConfigurationException | NoConfigurationFileException e) {
            throw new RuntimeException(e);
        }
        PeerNavigationService.loadConfiguration(getProject(), peerNavigationConfiguration);
    }
}
