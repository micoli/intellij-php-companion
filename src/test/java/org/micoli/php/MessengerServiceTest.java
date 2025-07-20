package org.micoli.php;

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
import org.micoli.php.symfony.messenger.service.MessengerService;

public class MessengerServiceTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData";
    }

    public void testItDetectMessageBasedOnPatternClass() {
        myFixture.configureByFile("/src/Core/Event/ArticleCreatedEvent.php");
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = new SymfonyMessengerConfiguration();
        symfonyMessengerConfiguration.messageClassNamePatterns = ".*(edEvent|Command)$";
        MessengerService messengerService = MessengerService.getInstance(getProject());
        messengerService.loadConfiguration(myFixture.getProject(), symfonyMessengerConfiguration);
        PhpClass phpClass = PhpUtil.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertNotNull(phpClass);
        assertTrue(messengerService.isMessageClass(phpClass));
    }

    public void testItDoesNotDetectMessageBasedOnPatternIfPatternIsWrong() {
        myFixture.configureByFile("/src/Core/Event/ArticleCreatedEvent.php");
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = new SymfonyMessengerConfiguration();
        symfonyMessengerConfiguration.messageClassNamePatterns = ".*(Command)$";
        MessengerService messengerService = MessengerService.getInstance(getProject());
        messengerService.loadConfiguration(myFixture.getProject(), symfonyMessengerConfiguration);
        PhpClass phpClass = PhpUtil.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertNotNull(phpClass);
        assertFalse(messengerService.isMessageClass(phpClass));
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
        MessengerService messengerService = MessengerService.getInstance(getProject());
        messengerService.loadConfiguration(myFixture.getProject(), symfonyMessengerConfiguration);
        PhpClass phpClass = PhpUtil.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertNotNull(phpClass);
        assertTrue(messengerService.isMessageClass(phpClass));
    }

    public void testItCanFindHandlersByMessage() {
        myFixture.copyDirectoryToProject("src", "src");
        MessengerService messengerService = loadPluginConfiguration(getTestDataPath());
        Collection<Method> handledMessages =
                messengerService.findHandlersByMessageName("App\\Core\\Event\\ArticleCreatedEvent");
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
        MessengerService messengerService = loadPluginConfiguration(getTestDataPath());
        Collection<MethodReference> callsWithoutRootNamespace =
                messengerService.findDispatchCallsForMessage("App\\Core\\Event\\ArticleCreatedEvent");
        Collection<MethodReference> callsWithRootNamespace =
                messengerService.findDispatchCallsForMessage("\\App\\Core\\Event\\ArticleCreatedEvent");

        assertEquals(1, callsWithoutRootNamespace.size());
        assertEquals(callsWithRootNamespace.size(), callsWithoutRootNamespace.size());
    }

    private MessengerService loadPluginConfiguration(String path) {
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = null;
        try {
            symfonyMessengerConfiguration = Objects.requireNonNull(ConfigurationFactory.loadConfiguration(path, 0L))
                    .configuration
                    .symfonyMessenger;
        } catch (ConfigurationException | NoConfigurationFileException e) {
            throw new RuntimeException(e);
        }
        MessengerService messengerService = MessengerService.getInstance(getProject());
        messengerService.loadConfiguration(myFixture.getProject(), symfonyMessengerConfiguration);

        return messengerService;
    }
}
