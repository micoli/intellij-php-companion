package org.micoli.php;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.micoli.php.configuration.ConfigurationException;
import org.micoli.php.configuration.ConfigurationFactory;
import org.micoli.php.configuration.NoConfigurationFileException;
import org.micoli.php.symfony.list.RouteElementDTO;
import org.micoli.php.symfony.list.RouteService;
import org.micoli.php.symfony.list.configuration.RoutesConfiguration;

public class RouteListTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData";
    }

    public void testItGetRoutesFromAttributes() {
        myFixture.copyDirectoryToProject("src", "/src");
        RouteService routeListService = loadPluginConfiguration(getTestDataPath());
        List<RouteElementDTO> lists = routeListService.getRoutes();
        String formattedList = lists.stream().map(it -> it.uri()).sorted().collect(Collectors.joining(","));
        String expectedList = new ArrayList<>(Arrays.asList(
                        "/api/article/{articleId}",
                        "/api/articles/feed/{feedId}",
                        "/api/articles/list",
                        "/api/articles/tag/{tag}",
                        "/api/articles/user"))
                .stream().sorted().collect(Collectors.joining(","));
        assertEquals(expectedList, formattedList);
    }

    private RouteService loadPluginConfiguration(String path) {
        RoutesConfiguration routeListConfiguration = null;
        try {
            routeListConfiguration = Objects.requireNonNull(ConfigurationFactory.loadConfiguration(path, 0L))
                    .configuration
                    .routesConfiguration;
        } catch (ConfigurationException | NoConfigurationFileException e) {
            throw new RuntimeException(e);
        }
        RouteService instance = RouteService.getInstance(getProject());
        instance.loadConfiguration(myFixture.getProject(), routeListConfiguration);

        return instance;
    }
}
