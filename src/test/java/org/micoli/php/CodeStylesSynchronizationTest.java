package org.micoli.php;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.concurrent.atomic.AtomicReference;
import org.micoli.php.builders.CodeStyleBuilder;
import org.micoli.php.builders.CodeStylesSynchronizationConfigurationBuilder;
import org.micoli.php.codeStyle.CodeStylesService;
import org.micoli.php.codeStyle.configuration.CodeStyle;
import org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration;
import org.micoli.php.ui.Notification;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class CodeStylesSynchronizationTest extends BasePlatformTestCase {
    AtomicReference<String> lastError = new AtomicReference<>();
    AtomicReference<String> lastMessage = new AtomicReference<>();

    public void testItSetCodeStyleAndReportsErrorIfAny() {
        try (MockedStatic<Notification> mockedStatic = Mockito.mockStatic(Notification.class)) {
            mockedStatic
                    .when(() -> Notification.error(Mockito.anyString(), Mockito.anyString()))
                    .thenAnswer(invocation -> {
                        lastError.set(invocation.getArgument(1));
                        return null;
                    });
            mockedStatic
                    .when(() -> Notification.message(Mockito.anyString(), Mockito.anyString()))
                    .thenAnswer(invocation -> {
                        lastMessage.set(invocation.getArgument(1));
                        return null;
                    });
            lastError.set(null);
            lastMessage.set(null);
            // Given
            loadPluginConfiguration(CodeStylesSynchronizationConfigurationBuilder.create()
                    .withEnabled(true)
                    .withCodeStyles(new CodeStyle[] {
                        CodeStyleBuilder.create()
                                .withStyleAttribute("UNKNOWN_STYLE_ATTRIBUTE")
                                .withValue("true")
                                .build(),
                        CodeStyleBuilder.create()
                                .withStyleAttribute("ALIGN_MULTILINE_PARAMETERS_IN_CALLS")
                                .withValue("true")
                                .build()
                    })
                    .build());

            // Then
            assertEquals("<html><ul><li>Unknown attribute UNKNOWN_STYLE_ATTRIBUTE</li></ul></html>", lastError.get());
            assertEquals("<html><ul><li>ALIGN_MULTILINE_PARAMETERS_IN_CALLS: true</li></ul></html>", lastMessage.get());

            // And rollback
            loadPluginConfiguration(CodeStylesSynchronizationConfigurationBuilder.create()
                    .withEnabled(true)
                    .withCodeStyles(new CodeStyle[] {
                        CodeStyleBuilder.create()
                                .withStyleAttribute("ALIGN_MULTILINE_PARAMETERS_IN_CALLS")
                                .withValue("false")
                                .build()
                    })
                    .build());
        }
    }

    public void testItMustNoSetPropertyIfAlreadySet() {
        try (MockedStatic<Notification> mockedStatic = Mockito.mockStatic(Notification.class)) {
            mockedStatic
                    .when(() -> Notification.error(Mockito.anyString(), Mockito.anyString()))
                    .thenAnswer(invocation -> {
                        lastError.set(invocation.getArgument(1));
                        return null;
                    });
            mockedStatic
                    .when(() -> Notification.message(Mockito.anyString(), Mockito.anyString()))
                    .thenAnswer(invocation -> {
                        lastMessage.set(invocation.getArgument(1));
                        return null;
                    });
            lastError.set(null);
            lastMessage.set(null);

            // Given
            loadPluginConfiguration(CodeStylesSynchronizationConfigurationBuilder.create()
                    .withEnabled(true)
                    .withCodeStyles(new CodeStyle[] {
                        CodeStyleBuilder.create()
                                .withStyleAttribute("ALIGN_MULTILINE_PARAMETERS_IN_CALLS")
                                .withValue("false")
                                .build()
                    })
                    .build());

            // Then
            assertNull(lastMessage.get());
            assertNull(lastError.get());
        }
    }

    private void loadPluginConfiguration(CodeStylesSynchronizationConfiguration configuration) {
        CodeStylesService instance = CodeStylesService.getInstance(getProject());
        instance.loadConfiguration(configuration);
    }
}
