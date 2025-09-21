package org.micoli.php

import com.google.common.io.Files
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Objects
import org.junit.Assert
import org.junit.Test
import org.micoli.php.configuration.ConfigurationException
import org.micoli.php.configuration.ConfigurationFactory
import org.micoli.php.configuration.exceptions.NoConfigurationFileException
import org.micoli.php.utils.YamlAssertUtils.assertYamlEquals
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

class ConfigurationFactoryTest {
    @Test
    fun testItReportEmptyConfiguration() {
        val exception = Assert.assertThrows(NoConfigurationFileException::class.java) { getLoadedConfiguration(getConfigurationPath("empty")) }
        Assert.assertSame("No .php-companion(.local).(json|yaml) configuration file(s) found.", exception.message)
    }

    @Test
    fun testItReportErroneousConfiguration() {
        testErroneousConfiguration("erroneousConfiguration", "Unrecognized Property: attributeNavigation.unknownSubProperty (line: 1, column: 57)")
    }

    @Test
    @Throws(Exception::class)
    fun testItReportExtraPropertiesButLoadConfiguration() {
        testSuccessfulConfiguration("allowExtraMainProperties", mutableListOf("aa (line: 1, column: 7)", "aa.test (line: 1, column: 15)"))
    }

    @Test
    @Throws(Exception::class)
    fun testItFailsWithExtraSubProperties() {
        testErroneousConfiguration("erroneousExtraSubProperties", "Unrecognized Property: attributeNavigation.unknownSubProperty (line: 1, column: 57)")
    }

    @Test
    fun testItReportMisspelledConfiguration1() {
        testErroneousConfiguration(
          "misspelledConfiguration1",
          "Unrecognized Property: attributeNavigation.rules.[0].propertyNameaa (line: 1, column: 117)," + " peerNavigation.associates.[0].classAZ (line: 1, column: 170)",
        )
    }

    @Test
    fun testItReportMisspelledConfiguration2() {
        testErroneousConfiguration("misspelledConfiguration2", "Mismatched Input: peerNavigation.associates")
    }

    @Test
    fun testItReportMisspelledConfiguration3() {
        testErroneousConfiguration(
          "misspelledConfiguration3",
          """
                mapping values are not allowed here
                 in 'reader', line 2, column 13:
                      associates:
                                ^

            """
            .trimIndent(),
        )
    }

    @Test
    @Throws(Exception::class)
    fun testItSucceedsToLoadASimpleYamlConfiguration() {
        testSuccessfulConfiguration("simpleYaml", ArrayList())
    }

    @Test
    @Throws(Exception::class)
    fun testItSucceedsToLoadMultipleYamlWithEmptyConfiguration() {
        testSuccessfulConfiguration("multipleYamlWithEmptyConfiguration", ArrayList())
    }

    @Test
    @Throws(Exception::class)
    fun testItSucceedsToLoadMultipleYamlAndOneWithEmptyConfiguration() {
        testSuccessfulConfiguration("multipleYamlAndOneWithEmptyConfiguration", ArrayList())
    }

    @Test
    @Throws(Exception::class)
    fun testItSucceedsToLoadAMultipleFileYamlConfiguration() {
        testSuccessfulConfiguration("multipleYaml", ArrayList())
    }

    private fun testErroneousConfiguration(configurationPath: String, expectedMessage: String) {
        val exception = Assert.assertThrows(ConfigurationException::class.java) { getLoadedConfiguration(getConfigurationPath(configurationPath)) }
        Assert.assertEquals(expectedMessage.trim { it <= ' ' }, exception.message!!.trim { it <= ' ' })
    }

    @Throws(ConfigurationException::class, IOException::class, NoConfigurationFileException::class)
    private fun testSuccessfulConfiguration(testPath: String, expectedIgnoredProperties: MutableList<String>) {
        // Given
        val file = getConfigurationPath(testPath)

        // When
        val createdConfiguration: ConfigurationFactory.LoadedConfiguration = checkNotNull(getLoadedConfiguration(file))
        // Then
        val dumperOptions = DumperOptions()
        dumperOptions.indent = 4
        dumperOptions.isPrettyFlow = true
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val loadedConfiguration = Yaml(dumperOptions).dump(createdConfiguration.configuration)
        val expectedConfiguration =
          Files.asCharSource(File(Objects.requireNonNull(javaClass.getClassLoader().getResource("configuration/$testPath/expect.yaml")).file), StandardCharsets.UTF_8).read()
        assertYamlEquals(expectedConfiguration, loadedConfiguration)
        Assert.assertEquals(expectedIgnoredProperties.joinToString(","), createdConfiguration.ignoredProperties.joinToString(","))
    }

    @Throws(ConfigurationException::class, NoConfigurationFileException::class)
    private fun getLoadedConfiguration(file: File): ConfigurationFactory.LoadedConfiguration? {
        return ConfigurationFactory.loadConfiguration(file.absolutePath, 0L, true)
    }

    private fun getConfigurationPath(path: String): File {
        return File(Objects.requireNonNull(javaClass.getClassLoader().getResource("configuration/$path")).file)
    }
}
