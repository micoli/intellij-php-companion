package org.micoli.php.symfony.ParseCliDumper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(Parameterized.class)
public class JsonToPhpArrayConverterTest {

    private static final String directory = "src/test/java/org/micoli/php/symfony/ParseCliDumper/JsonToPhpArrayConverterTestCases/";
    private final String filename;

    public JsonToPhpArrayConverterTest(String filename) {
        this.filename = filename;
    }

    @Parameterized.Parameters(name = "testItParseDump({0})")
    public static Collection<Object[]> data() throws IOException {
        // spotless:off
        try (Stream<Path> stream = Files.list(Paths.get(directory))) {
            return stream.filter(path -> path.toString().endsWith(".json"))
                .map(path -> new Object[]{path.getFileName().toString().replace(".json", "")})
                .collect(Collectors.toList());
        }
        // spotless:on
    }

    @Test
    public void testItConvertJsonToPhp() throws IOException {
        // spotless:off
        System.out.println(JsonToPhpArrayConverter.convertJsonToPhp(readFile(".json")));
        assert(readFile(".php").equals(JsonToPhpArrayConverter.convertJsonToPhp(readFile(".json"))));
        // spotless:on
    }

    private String readFile(String path) throws IOException {
        return String.join("\n", Files.readAllLines(Paths.get(directory + filename + path), StandardCharsets.UTF_8));
    }
}
