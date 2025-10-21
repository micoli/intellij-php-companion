package org.micoli.php

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.replaceService
import java.util.concurrent.atomic.AtomicReference
import org.assertj.core.api.Assertions.*
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
        assertThat(lastError.get())
            .isEqualTo(
                "<html><ul><li>Unknown attribute UNKNOWN_STYLE_ATTRIBUTE</li></ul></html>",
            )
        assertThat(lastMessage.get())
            .isEqualTo(
                "<html><ul><li>ALIGN_MULTILINE_PARAMETERS_IN_CALLS: true</li></ul></html>",
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
        assertThat(lastMessage.get()).isNull()
        assertThat(lastError.get()).isNull()
    }

    private fun loadPluginConfiguration(configuration: CodeStylesSynchronizationConfiguration?) {
        val instance = CodeStylesService.getInstance(project)
        instance.loadConfiguration(configuration)
    }
}
