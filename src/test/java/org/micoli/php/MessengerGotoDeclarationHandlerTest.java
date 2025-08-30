package org.micoli.php;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.Objects;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.exceptions.NoConfigurationFileException;
import org.micoli.php.symfony.messenger.navigation.MessengerGotoDeclarationHandler;
import org.micoli.php.symfony.messenger.service.MessengerService;

public class MessengerGotoDeclarationHandlerTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData/";
    }

    public void testItDetectDispatchMethods() throws NoConfigurationFileException, ConfigurationException {
        myFixture.copyDirectoryToProject("src", "/src");
        MessengerService.getInstance(getProject())
                .loadConfiguration(
                        myFixture.getProject(),
                        Objects.requireNonNull(ConfigurationFactory.loadConfiguration(getTestDataPath(), 0L))
                                .configuration
                                .symfonyMessenger);
        PsiFile[] files = myFixture.configureByFiles("src/UserInterface/Web/Api/Article/List/Controller.php");
        assertGotoEquals(files[0], "->query(", "src/Core/Query/Article/Handler.php", "public function __invoke");
        assertGotoEquals(files[0], "->notify(", "src/Core/Query/Article/Handler.php", "public function __invoke");
        assertGotoEquals(
                files[0],
                "->query(new ArticleDetails\\Query())",
                "src/Core/Query/ArticleDetails/Handler.php",
                "public function __invoke");
        assertGotoIsNull(files[0], "->handle(");
        assertGotoIsNull(files[0], "->dispatch");
        assertGotoIsNull(files[0], "->queryBus");
    }

    private void assertGotoIsNull(PsiFile file, String elementMatch) {
        int pos = file.getText().indexOf(elementMatch);
        PsiElement element = file.findElementAt(pos + 2);
        MessengerGotoDeclarationHandler messengerGotoDeclarationHandler = new MessengerGotoDeclarationHandler();
        assertNull(messengerGotoDeclarationHandler.getGotoDeclarationTargets(element, 0, myFixture.getEditor()));
    }

    private void assertGotoEquals(PsiFile file, String elementMatch, String targetFileEnd, String targetMethodStart) {
        int pos = file.getText().indexOf(elementMatch);
        PsiElement element = file.findElementAt(pos + 2);
        MessengerGotoDeclarationHandler messengerGotoDeclarationHandler = new MessengerGotoDeclarationHandler();
        PsiElement[] foundElements =
                messengerGotoDeclarationHandler.getGotoDeclarationTargets(element, 0, myFixture.getEditor());
        assertNotNull(foundElements);
        assertEquals(1, foundElements.length);
        assertTrue(foundElements[0]
                .getContainingFile()
                .getVirtualFile()
                .getCanonicalPath()
                .endsWith(targetFileEnd));
        assertTrue(foundElements[0].getText().startsWith(targetMethodStart));
    }
}
