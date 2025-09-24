package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.replaceService
import java.util.concurrent.atomic.AtomicReference
import junit.framework.TestCase
import org.micoli.php.builders.CodeStyleBuilder
import org.micoli.php.builders.CodeStylesSynchronizationConfigurationBuilder
import org.micoli.php.codeStyle.CodeStylesService
import org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration
import org.micoli.php.ui.Notification

class CodeStylesSynchronizationTest : BasePlatformTestCase() {
    fun testItSetCodeStyleAndReportsErrorIfAny() {
        val lastError: AtomicReference<String?> = AtomicReference<String?>()
        val lastMessage: AtomicReference<String?> = AtomicReference<String?>()
        val mockAppService: Notification =
            object : Notification(project) {
                override fun error(title: String, message: String) {
                    lastError.set(message)
                }

                override fun message(title: String, message: String) {
                    lastMessage.set(message)
                }
            }
        project.replaceService(Notification::class.java, mockAppService, testRootDisposable)
        lastError.set(null)
        lastMessage.set(null)
        // Given
        loadPluginConfiguration(
            CodeStylesSynchronizationConfigurationBuilder.create()
                .withEnabled(true)
                .withCodeStyles(
                    arrayOf(
                        CodeStyleBuilder.create()
                            .withStyleAttribute("UNKNOWN_STYLE_ATTRIBUTE")
                            .withValue("true")
                            .build(),
                        CodeStyleBuilder.create()
                            .withStyleAttribute("ALIGN_MULTILINE_PARAMETERS_IN_CALLS")
                            .withValue("true")
                            .build(),
                    ))
                .build())

        // Then
        TestCase.assertEquals(
            "<html><ul><li>Unknown attribute UNKNOWN_STYLE_ATTRIBUTE</li></ul></html>",
            lastError.get(),
        )
        TestCase.assertEquals(
            "<html><ul><li>ALIGN_MULTILINE_PARAMETERS_IN_CALLS: true</li></ul></html>",
            lastMessage.get(),
        )

        // And rollback
        loadPluginConfiguration(
            CodeStylesSynchronizationConfigurationBuilder.create()
                .withEnabled(true)
                .withCodeStyles(
                    arrayOf(
                        CodeStyleBuilder.create()
                            .withStyleAttribute("ALIGN_MULTILINE_PARAMETERS_IN_CALLS")
                            .withValue("false")
                            .build()))
                .build())
    }

    fun testItMustNoSetPropertyIfAlreadySet() {
        val lastError: AtomicReference<String?> = AtomicReference<String?>()
        val lastMessage: AtomicReference<String?> = AtomicReference<String?>()
        val mockAppService: Notification =
            object : Notification(project) {
                override fun error(title: String, message: String) {
                    lastError.set(message)
                }

                override fun message(title: String, message: String) {
                    lastMessage.set(message)
                }
            }
        project.replaceService(Notification::class.java, mockAppService, testRootDisposable)

        lastError.set(null)
        lastMessage.set(null)

        // Given
        loadPluginConfiguration(
            CodeStylesSynchronizationConfigurationBuilder.create()
                .withEnabled(true)
                .withCodeStyles(
                    arrayOf(
                        CodeStyleBuilder.create()
                            .withStyleAttribute("ALIGN_MULTILINE_PARAMETERS_IN_CALLS")
                            .withValue("false")
                            .build()))
                .build())

        // Then
        assertNull(lastMessage.get())
        assertNull(lastError.get())
    }

    private fun loadPluginConfiguration(configuration: CodeStylesSynchronizationConfiguration?) {
        val instance = CodeStylesService.getInstance(project)
        instance.loadConfiguration(configuration)
    }
}
