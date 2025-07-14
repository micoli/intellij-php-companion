package org.micoli.php.symfony.messenger.service;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.NoConfigurationFileException;
import org.micoli.php.service.PhpUtil;
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration;

public class MessengerServiceTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testItDetectMessageBasedOnPatternClass() {
        myFixture.configureByFile("/src/Core/Event/ArticleCreatedEvent.php");
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = new SymfonyMessengerConfiguration();
        symfonyMessengerConfiguration.messageClassNamePatterns = ".*(edEvent|Command)$";
        MessengerServiceConfiguration.loadConfiguration(symfonyMessengerConfiguration);
        PhpClass phpClass = PhpUtil.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertNotNull(phpClass);
        assertTrue(MessengerService.isMessageClass(phpClass));
    }

    public void testItDoesNotDetectMessageBasedOnPatternIfPatternIsWrong() {
        myFixture.configureByFile("/src/Core/Event/ArticleCreatedEvent.php");
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = new SymfonyMessengerConfiguration();
        symfonyMessengerConfiguration.messageClassNamePatterns = ".*(Command)$";
        MessengerServiceConfiguration.loadConfiguration(symfonyMessengerConfiguration);
        PhpClass phpClass = PhpUtil.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertNotNull(phpClass);
        assertFalse(MessengerService.isMessageClass(phpClass));
    }

    public void testItDetectMessageBasedOnInterface() {
        myFixture.configureByFiles(
                "/src/Core/Event/ArticleCreatedEvent.php",
                "/src/Infrastructure/Bus/Message/Event/AsyncEventInterface.php",
                "/src/Infrastructure/Bus/Message/Event/EventInterface.php",
                "/src/Infrastructure/Bus/Message/MessageInterface.php");
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = new SymfonyMessengerConfiguration();
        symfonyMessengerConfiguration.messageInterfaces =
                new String[] {"App\\Infrastructure\\Bus\\Message\\MessageInterface"};
        MessengerServiceConfiguration.loadConfiguration(symfonyMessengerConfiguration);
        PhpClass phpClass = PhpUtil.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertNotNull(phpClass);
        assertTrue(MessengerService.isMessageClass(phpClass));
    }

    public void testItCanFindHandlersByMessage() {
        myFixture.copyDirectoryToProject("src", "src");
        loadPluginConfiguration(getTestDataPath());
        Collection<Method> handledMessages =
                MessengerService.findHandlersByMessageName(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertContainsElements(
                handledMessages.stream().map(PhpNamedElement::getFQN).toList(),
                "\\App\\Core\\EventListener\\OnArticleCreated.__invoke");
    }

    public void testItCanFindLineMarkersForMessageHandler() {
        myFixture.copyDirectoryToProject("src", "src");
        myFixture.configureByFiles("src/Core/EventListener/OnFeedCreated.php");
        loadPluginConfiguration(getTestDataPath());
        List<GutterMark> lineMarkers = myFixture.findAllGutters();
        assertNotEmpty(lineMarkers);

        List<GutterMark> specificMarkers = lineMarkers.stream()
                .filter(it -> {
                    String tooltipText = it.getTooltipText();
                    if (tooltipText == null) {
                        return false;
                    }
                    return tooltipText.contains("Search for usages")
                            || tooltipText.contains("Navigate to message handlers");
                })
                .toList();
        assertEquals(2, specificMarkers.size());
    }

    public void testItCanFindDispatchCallsForMessageClass() {
        myFixture.copyDirectoryToProject("src", "src");
        loadPluginConfiguration(getTestDataPath());
        Collection<MethodReference> callsWithoutRootNamespace =
                MessengerService.findDispatchCallsForMessage(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        Collection<MethodReference> callsWithRootNamespace =
                MessengerService.findDispatchCallsForMessage(getProject(), "\\App\\Core\\Event\\ArticleCreatedEvent");

        assertEquals(1, callsWithoutRootNamespace.size());
        assertEquals(callsWithRootNamespace.size(), callsWithoutRootNamespace.size());
    }

    private void loadPluginConfiguration(String path) {
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = null;
        try {
            symfonyMessengerConfiguration = Objects.requireNonNull(ConfigurationFactory.loadConfiguration(path, 0L))
                    .configuration
                    .symfonyMessenger;
        } catch (ConfigurationException | NoConfigurationFileException e) {
            throw new RuntimeException(e);
        }
        MessengerServiceConfiguration.loadConfiguration(symfonyMessengerConfiguration);
    }
}
