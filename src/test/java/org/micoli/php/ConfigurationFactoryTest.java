package org.micoli.php;

import static org.junit.Assert.assertThrows;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.exceptions.NoConfigurationFileException;
import org.micoli.php.utils.YamlAssertUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class ConfigurationFactoryTest {
    @Test
    public void testItReportEmptyConfiguration() {
        NoConfigurationFileException exception = assertThrows(
                NoConfigurationFileException.class, () -> getLoadedConfiguration(getConfigurationPath("empty")));
        Assert.assertSame("No .php-companion(.local).(json|yaml) configuration file(s) found.", exception.getMessage());
    }

    @Test
    public void testItReportErroneousConfiguration() {
        testErroneousConfiguration(
                "erroneousConfiguration",
                "Unrecognized Property: attributeNavigation.unknownSubProperty (line: 1, column: 57)");
    }

    @Test
    public void testItReportExtraPropertiesButLoadConfiguration() throws Exception {
        testSuccessfulConfiguration(
                "allowExtraMainProperties", List.of("aa (line: 1, column: 7)", "aa.test (line: 1, column: 15)"));
    }

    @Test
    public void testItFailsWithExtraSubProperties() throws Exception {
        testErroneousConfiguration(
                "erroneousExtraSubProperties",
                "Unrecognized Property: attributeNavigation.unknownSubProperty (line: 1, column: 57)");
    }

    @Test
    public void testItReportMisspelledConfiguration1() {
        testErroneousConfiguration(
                "misspelledConfiguration1",
                "Unrecognized Property: attributeNavigation.rules.[0].propertyNameaa (line: 1, column: 117), peerNavigation.associates.[0].classAZ (line: 1, column: 170)");
    }

    @Test
    public void testItReportMisspelledConfiguration2() {
        testErroneousConfiguration("misspelledConfiguration2", "Mismatched Input: peerNavigation.associates");
    }

    @Test
    public void testItReportMisspelledConfiguration3() {
        testErroneousConfiguration(
                "misspelledConfiguration3",
                """
                mapping values are not allowed here
                 in 'reader', line 2, column 13:
                      associates:
                                ^
                """);
    }

    @Test
    public void testItSucceedsToLoadASimpleYamlConfiguration() throws Exception {
        testSuccessfulConfiguration("simpleYaml", new ArrayList<>());
    }

    @Test
    public void testItSucceedsToLoadMultipleYamlWithEmptyConfiguration() throws Exception {
        testSuccessfulConfiguration("multipleYamlWithEmptyConfiguration", new ArrayList<>());
    }

    @Test
    public void testItSucceedsToLoadMultipleYamlAndOneWithEmptyConfiguration() throws Exception {
        testSuccessfulConfiguration("multipleYamlAndOneWithEmptyConfiguration", new ArrayList<>());
    }

    @Test
    public void testItSucceedsToLoadAMultipleFileYamlConfiguration() throws Exception {
        testSuccessfulConfiguration("multipleYaml", new ArrayList<>());
    }

    private void testErroneousConfiguration(String configurationPath, String expectedMessage) {
        ConfigurationException exception = assertThrows(
                ConfigurationException.class, () -> getLoadedConfiguration(getConfigurationPath(configurationPath)));
        Assert.assertEquals(expectedMessage.trim(), exception.getMessage().trim());
    }

    private void testSuccessfulConfiguration(String testPath, List<String> expectedIgnoredProperties)
            throws ConfigurationException, IOException, NoConfigurationFileException {
        // Given
        File file = getConfigurationPath(testPath);

        // When
        ConfigurationFactory.LoadedConfiguration createdConfiguration = getLoadedConfiguration(file);
        assert (createdConfiguration != null);

        // Then
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setIndent(4);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String loadedConfiguration = new Yaml(dumperOptions).dump(createdConfiguration.configuration);
        String expectedConfiguration = Files.asCharSource(
                        new File(Objects.requireNonNull(getClass()
                                        .getClassLoader()
                                        .getResource("configuration/" + testPath + "/expect.yaml"))
                                .getFile()),
                        StandardCharsets.UTF_8)
                .read();
        YamlAssertUtils.assertYamlEquals(expectedConfiguration, loadedConfiguration);
        Assert.assertEquals(
                String.join(",", expectedIgnoredProperties), String.join(",", createdConfiguration.ignoredProperties));
    }

    private ConfigurationFactory.@Nullable LoadedConfiguration getLoadedConfiguration(File file)
            throws ConfigurationException, NoConfigurationFileException {
        return ConfigurationFactory.loadConfiguration(file.getAbsolutePath(), 0L, true);
    }

    @NotNull
    private File getConfigurationPath(String path) {
        return new File(Objects.requireNonNull(getClass().getClassLoader().getResource("configuration/" + path))
                .getFile());
    }
}
