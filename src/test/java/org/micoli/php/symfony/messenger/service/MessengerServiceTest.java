package org.micoli.php.symfony.messenger.service;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import java.util.Collection;
import java.util.Objects;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.NoConfigurationFileException;
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
        PhpClass phpClass = PHPHelper.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertTrue(MessengerService.isMessageClass(phpClass));
    }

    public void testItDoesNotDetectMessageBasedOnPatternIfPatternIsWrong() {
        myFixture.configureByFile("/src/Core/Event/ArticleCreatedEvent.php");
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = new SymfonyMessengerConfiguration();
        symfonyMessengerConfiguration.messageClassNamePatterns = ".*(Command)$";
        MessengerServiceConfiguration.loadConfiguration(symfonyMessengerConfiguration);
        PhpClass phpClass = PHPHelper.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertFalse(MessengerService.isMessageClass(phpClass));
    }

    public void testItDetectMessageBasedOnInterface() {
        myFixture.configureByFiles(
        "/src/Core/Event/ArticleCreatedEvent.php",
        "/src/Infrastructure/Bus/Message/Event/AsyncEventInterface.php",
        "/src/Infrastructure/Bus/Message/Event/EventInterface.php",
        "/src/Infrastructure/Bus/Message/MessageInterface.php"
        );
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = new SymfonyMessengerConfiguration();
        symfonyMessengerConfiguration.messageInterfaces = new String[]{"App\\Infrastructure\\Bus\\Message\\MessageInterface"};
        MessengerServiceConfiguration.loadConfiguration(symfonyMessengerConfiguration);
        PhpClass phpClass = PHPHelper.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertTrue(MessengerService.isMessageClass(phpClass));
    }

    public void testItCanFindHandlersByMessage() {
        myFixture.copyDirectoryToProject("src", "src");
        loadPluginConfiguration(getTestDataPath());
        Collection<Method> handledMessages = MessengerService.findHandlersByMessageName(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertContainsElements(handledMessages.stream().map(PhpNamedElement::getFQN).toList(), "\\App\\Core\\EventListener\\OnArticleCreated.__invoke");
    }

    private void loadPluginConfiguration(String path) {
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = null;
        try {
            symfonyMessengerConfiguration = Objects.requireNonNull(ConfigurationFactory.loadConfiguration(path, 0L)).configuration.symfonyMessenger;
        } catch (ConfigurationException | NoConfigurationFileException e) {
            throw new RuntimeException(e);
        }
        MessengerServiceConfiguration.loadConfiguration(symfonyMessengerConfiguration);
    }
}
