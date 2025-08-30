package org.micoli.php;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.exceptions.NoConfigurationFileException;
import org.micoli.php.symfony.list.DoctrineEntityElementDTO;
import org.micoli.php.symfony.list.DoctrineEntityService;
import org.micoli.php.symfony.list.configuration.DoctrineEntitiesConfiguration;

public class DoctrineEntityListTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData";
    }

    public void testItGetRoutesFromAttributes() {
        myFixture.copyDirectoryToProject("src", "/src");
        DoctrineEntityService DoctrineEntityListService = loadPluginConfiguration(getTestDataPath());
        List<DoctrineEntityElementDTO> lists = DoctrineEntityListService.getElements();
        String formattedList =
                lists.stream().map(DoctrineEntityElementDTO::name).sorted().collect(Collectors.joining(","));
        String expectedList = new ArrayList<>(Arrays.asList("article__article", "article__feed"))
                .stream().sorted().collect(Collectors.joining(","));
        assertEquals(expectedList, formattedList);
    }

    private DoctrineEntityService loadPluginConfiguration(String path) {
        DoctrineEntitiesConfiguration doctrineEntityListConfiguration = null;
        try {
            doctrineEntityListConfiguration = Objects.requireNonNull(ConfigurationFactory.loadConfiguration(path, 0L))
                    .configuration
                    .doctrineEntitiesConfiguration;
        } catch (ConfigurationException | NoConfigurationFileException e) {
            throw new RuntimeException(e);
        }
        DoctrineEntityService instance = DoctrineEntityService.getInstance(getProject());
        instance.loadConfiguration(myFixture.getProject(), doctrineEntityListConfiguration);

        return instance;
    }
}
