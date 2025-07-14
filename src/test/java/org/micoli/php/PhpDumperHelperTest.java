package org.micoli.php;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.micoli.php.symfony.cliDumpParser.PhpDumpHelper;
import org.micoli.php.utils.JsonAssertUtils;

@RunWith(Parameterized.class)
public class PhpDumperHelperTest {

    private static final String directory = "src/test/resources/phpDumpHelperTestCases/";
    private final String filename;

    public PhpDumperHelperTest(String filename) {
        this.filename = filename;
    }

    @Parameterized.Parameters(name = "testItParseDump({0})")
    public static Collection<Object[]> data() throws IOException {

        try (Stream<Path> stream = Files.list(Paths.get(directory))) {
            return stream.filter(path -> path.toString().endsWith(".txt"))
                    .map(path -> new Object[] {path.getFileName().toString().replace(".txt", "")})
                    .collect(Collectors.toList());
        }
    }

    @Test
    public void testItParseDump() throws IOException {

        JsonAssertUtils.assertJsonEquals(readFile(".json"), PhpDumpHelper.parseCliDumperToJson(readFile(".txt")));
    }

    private String readFile(String path) throws IOException {
        return String.join("\n", Files.readAllLines(Paths.get(directory + filename + path), StandardCharsets.UTF_8));
    }
}
