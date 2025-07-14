package org.micoli.php;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.Yaml;

public class YamlAssertUtils {

    public static void assertYamlEquals(String expectedElement, String actualElement) throws IOException {
        Yaml yaml = new Yaml();
        expectedElement = filterYamlString(expectedElement);
        actualElement = filterYamlString(actualElement);
        List<String> differences = compareFilesWithDiff(expectedElement, actualElement);
        if (!differences.isEmpty()) {
            String message = String.format(
                    "JSON strings are not equal:\n--------------\n%s\n-----\nExpected:\n%s\n\nActual:\n%s",
                    String.join("\n", differences), expectedElement, actualElement);
            throw new AssertionError(message);
        }
    }

    private static String filterYamlString(String yamlString) throws IOException {
        return Arrays.stream(yamlString.split("\\n"))
                .filter(line -> !line.startsWith("!!"))
                .filter(line -> !line.endsWith(": []"))
                .filter(line -> !line.endsWith(": null"))
                .collect(Collectors.joining("\n"));
    }

    public static List<String> compareFilesWithDiff(String yamlA, String yamlB) throws IOException {
        Yaml yaml = new Yaml();
        Object yaml1 = yaml.load(yamlA);
        Object yaml2 = yaml.load(yamlB);
        List<String> differences = new ArrayList<>();
        compareObjectsWithDiff(yaml1, yaml2, "", differences);
        return differences;
    }

    private static boolean compareObjects(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }

        if (obj1 instanceof Map && obj2 instanceof Map) {
            return compareMaps((Map<?, ?>) obj1, (Map<?, ?>) obj2);
        }
        if (obj1 instanceof List && obj2 instanceof List) {
            return compareLists((List<?>) obj1, (List<?>) obj2);
        }
        if (obj1 instanceof Number && obj2 instanceof Number) {
            return obj1.toString().equals(obj2.toString());
        }
        return obj1.equals(obj2);
    }

    private static boolean compareMaps(Map<?, ?> map1, Map<?, ?> map2) {
        if (map1.size() != map2.size()) {
            return false;
        }

        for (Map.Entry<?, ?> entry : map1.entrySet()) {
            Object key = entry.getKey();
            if (!map2.containsKey(key)) {
                return false;
            }
            if (!compareObjects(entry.getValue(), map2.get(key))) {
                return false;
            }
        }
        return true;
    }

    private static boolean compareLists(List<?> list1, List<?> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        List<?> remainingItems = new ArrayList<>(list2);

        for (Object item1 : list1) {
            boolean found = false;
            Iterator<?> iterator = remainingItems.iterator();

            while (iterator.hasNext()) {
                Object item2 = iterator.next();
                if (compareObjects(item1, item2)) {
                    iterator.remove();
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return remainingItems.isEmpty();
    }

    private static void compareObjectsWithDiff(Object obj1, Object obj2, String path, List<String> differences) {
        if (obj1 == null && obj2 == null) {
            return;
        }
        if (obj1 == null) {
            differences.add(String.format("Path [%s] : missing value in first file", path));
            return;
        }
        if (obj2 == null) {
            differences.add(String.format("Path [%s] : missing value in second file", path));
            return;
        }

        if (obj1 instanceof Map && obj2 instanceof Map) {
            compareMapsWithDiff((Map<String, Object>) obj1, (Map<String, Object>) obj2, path, differences);
        } else if (obj1 instanceof List && obj2 instanceof List) {
            compareListsWithDiff((List<?>) obj1, (List<?>) obj2, path, differences);
        } else if (obj1 instanceof Number && obj2 instanceof Number) {
            if (!obj1.toString().equals(obj2.toString())) {
                differences.add(String.format("Path [%s] : values differs (%s != %s)", path, obj1, obj2));
            }
        } else if (!obj1.equals(obj2)) {
            differences.add(String.format("Path [%s] : values differs (%s != %s)", path, obj1, obj2));
        }
    }

    private static void compareMapsWithDiff(
            Map<String, Object> map1, Map<String, Object> map2, String path, List<String> differences) {
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(map1.keySet());
        allKeys.addAll(map2.keySet());

        for (String key : allKeys) {
            String currentPath = path.isEmpty() ? key : path + "." + key;

            if (!map1.containsKey(key)) {
                differences.add(String.format("path [%s] : missing key in first file", currentPath));
            } else if (!map2.containsKey(key)) {
                differences.add(String.format("path [%s] : missing key in second file", currentPath));
            } else {
                compareObjectsWithDiff(map1.get(key), map2.get(key), currentPath, differences);
            }
        }
    }

    private static void compareListsWithDiff(List<?> list1, List<?> list2, String path, List<String> differences) {
        if (list1.size() != list2.size()) {
            differences.add(String.format(
                    "Path [%s] : list size are different (first file: %d != second file: %d / [%s]/[%s])",
                    path,
                    list1.size(),
                    list2.size(),
                    list1.stream().map(Object::toString).collect(Collectors.joining(",")),
                    list2.stream().map(Object::toString).collect(Collectors.joining(","))));
            return;
        }

        List<?> remainingItems = new ArrayList<>(list2);

        for (int i = 0; i < list1.size(); i++) {
            Object item1 = list1.get(i);
            boolean found = false;

            for (int j = 0; j < remainingItems.size(); j++) {
                Object item2 = remainingItems.get(j);
                if (compareObjects(item1, item2)) {
                    remainingItems.remove(j);
                    found = true;
                    break;
                }
            }

            if (!found) {
                differences.add(String.format("Path [%s][%d] : missing element in second file", path, i));
            }
        }
    }
}
