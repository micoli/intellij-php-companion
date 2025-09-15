package org.micoli.php.configuration.schema.valueGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.jetbrains.annotations.NotNull;

public class IconValueGenerator implements PropertyValueGenerator {
    List<String> fieldNames = List.of("icon", "activeIcon", "inactiveIcon", "unknownIcon");

    @Override
    public ReferenceType getType() {
        return ReferenceType.AS_REF;
    }

    @Override
    public String getRefId() {
        return "icons";
    }

    @Override
    public List<String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public List<String> getValues() {
        return findAllExpUISVGResources();
    }

    public List<String> findAllExpUISVGResources() {
        List<String> resources = new ArrayList<>();
        final String EXP_UI_PATH = "expui";
        try {
            URL url = getClass().getClassLoader().getResource(EXP_UI_PATH);
            if (url == null) {
                return resources;
            }

            String protocol = url.getProtocol();
            switch (protocol) {
                case "file" -> {
                    Path basePath = Paths.get(url.toURI());
                    Files.walkFileTree(basePath, new SimpleFileVisitor<>() {
                        @Override
                        public @NotNull FileVisitResult visitFile(
                                @NotNull Path file, @NotNull BasicFileAttributes attrs) {
                            String filePath = file.toString();
                            if (!attrs.isDirectory()
                                    && filePath.startsWith(EXP_UI_PATH + "/")
                                    && filePath.endsWith(".svg")) {
                                resources.add(filePath);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
                case "jar" -> {
                    String jarPath = url.getPath()
                            .substring(EXP_UI_PATH.length(), url.getPath().indexOf("!"));
                    try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String filePath = entry.getName();
                            if (!entry.isDirectory()
                                    && filePath.startsWith(EXP_UI_PATH + "/")
                                    && filePath.endsWith(".svg")) {
                                resources.add(filePath);
                            }
                        }
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (URISyntaxException | IOException ignored) {
        }

        return resources.stream().filter(Objects::nonNull).distinct().sorted().toList();
    }
}
