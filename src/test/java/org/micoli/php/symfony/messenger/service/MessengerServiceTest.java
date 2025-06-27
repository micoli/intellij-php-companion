package org.micoli.php.symfony.messenger.service;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import java.util.Collection;
import java.util.Objects;
import org.junit.Test;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.NoConfigurationFileException;
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration;

public class MessengerServiceTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.copyDirectoryToProject("src", "src");
        //
        // myFixture.configureByFile("src/Core/Article/Application/Event/ArticleCreatedEvent.php");
        // PhpClass phpClass = myFixture.findElementByText("ArticleCreatedEvent",
        // PhpClass.class);
    }

    @Test
    public void testIsMessageClass() {
        SymfonyMessengerConfiguration symfonyMessengerConfiguration = new SymfonyMessengerConfiguration();
        // symfonyMessengerConfiguration.messageClassNamePatterns = "aaaaa";
        // symfonyMessengerConfiguration.messageInterfaces = new String[] { "aaaaa" };
        // symfonyMessengerConfiguration.dispatchMethods = new String[] { "aaaa" };
        MessengerServiceConfiguration.loadConfiguration(symfonyMessengerConfiguration);
        // loadPluginConfiguration(getTestDataPath());
        PhpClass phpClass = PHPHelper.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertNotNull(phpClass);
        assertTrue(MessengerService.isMessageClass(phpClass));
    }

    @Test
    public void testItCanFindHandlersByMessageName() {
        loadPluginConfiguration(getTestDataPath());
        PhpClass phpClass = PHPHelper.getPhpClassByFQN(getProject(), "App\\Core\\Event\\ArticleCreatedEvent");
        assertNotNull(phpClass);
        Collection<Method> handledMessages = MessengerService.findHandlersByMessageName(getProject(), phpClass.getFQN());
        assertContainsElements(handledMessages.stream().map(PhpNamedElement::getFQN).toList(), "\\App\\Core\\EventListener\\OnArticleCreated");
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
