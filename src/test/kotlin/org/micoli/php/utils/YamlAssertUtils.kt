package org.micoli.php.utils

import java.io.IOException
import java.util.Arrays
import java.util.stream.Collectors
import org.yaml.snakeyaml.Yaml

object YamlAssertUtils {
    @Throws(IOException::class)
    @JvmStatic
    fun assertYamlEquals(expectedElement: String, actualElement: String) {
        var expectedElement = expectedElement
        var actualElement = actualElement
        expectedElement = filterYamlString(expectedElement)
        actualElement = filterYamlString(actualElement)
        val differences = compareFilesWithDiff(expectedElement, actualElement)
        if (!differences.isEmpty()) {
            val message =
              String.format("JSON strings are not equal:\n--------------\n%s\n-----\nExpected:\n%s\n\nActual:\n%s", differences.joinToString("\n"), expectedElement, actualElement)
            throw AssertionError(message)
        }
    }

    @Throws(IOException::class)
    private fun filterYamlString(yamlString: String): String =
      Arrays.stream(yamlString.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        .filter { line: String? -> !line!!.startsWith("!!") }
        .filter { line: String? -> !line!!.endsWith(": []") }
        .filter { line: String? -> !line!!.endsWith(": null") }
        .collect(Collectors.joining("\n"))

    @Throws(IOException::class)
    fun compareFilesWithDiff(yamlA: String?, yamlB: String?): MutableList<String?> {
        val yaml = Yaml()
        val yaml1 = yaml.load<Any?>(yamlA)
        val yaml2 = yaml.load<Any?>(yamlB)
        val differences: MutableList<String?> = ArrayList()
        compareObjectsWithDiff(yaml1, yaml2, "", differences)
        return differences
    }

    private fun compareObjects(obj1: Any?, obj2: Any?): Boolean {
        if (obj1 == null && obj2 == null) {
            return true
        }
        if (obj1 == null || obj2 == null) {
            return false
        }

        if (obj1 is MutableMap<*, *> && obj2 is MutableMap<*, *>) {
            return compareMaps(obj1, obj2)
        }
        if (obj1 is MutableList<*> && obj2 is MutableList<*>) {
            return compareLists(obj1, obj2)
        }
        if (obj1 is Number && obj2 is Number) {
            return obj1.toString() == obj2.toString()
        }
        return obj1 == obj2
    }

    private fun compareMaps(map1: MutableMap<*, *>, map2: MutableMap<*, *>): Boolean {
        if (map1.size != map2.size) {
            return false
        }

        for (entry in map1.entries) {
            val key: Any? = entry.key
            if (!map2.containsKey(key)) {
                return false
            }
            if (!compareObjects(entry.value, map2[key])) {
                return false
            }
        }
        return true
    }

    private fun compareLists(list1: MutableList<*>, list2: MutableList<*>): Boolean {
        if (list1.size != list2.size) {
            return false
        }

        val remainingItems: MutableList<*> = ArrayList(list2)

        for (item1 in list1) {
            var found = false
            val iterator: MutableIterator<*> = remainingItems.iterator()

            while (iterator.hasNext()) {
                val item2 = iterator.next()
                if (compareObjects(item1, item2)) {
                    iterator.remove()
                    found = true
                    break
                }
            }

            if (!found) {
                return false
            }
        }

        return remainingItems.isEmpty()
    }

    private fun compareObjectsWithDiff(obj1: Any?, obj2: Any?, path: String, differences: MutableList<String?>) {
        if (obj1 == null && obj2 == null) {
            return
        }
        if (obj1 == null) {
            differences.add(String.format("Path [%s] : missing value in expected file", path))
            return
        }
        if (obj2 == null) {
            differences.add(String.format("Path [%s] : missing value in actual file", path))
            return
        }

        if (obj1 is MutableMap<*, *> && obj2 is MutableMap<*, *>) {
            compareMapsWithDiff(obj1 as MutableMap<String?, Any?>, obj2 as MutableMap<String?, Any?>, path, differences)
        } else if (obj1 is MutableList<*> && obj2 is MutableList<*>) {
            compareListsWithDiff(obj1, obj2, path, differences)
        } else if (obj1 is Number && obj2 is Number) {
            if (obj1.toString() != obj2.toString()) {
                differences.add(String.format("Path [%s] : values differs (%s != %s)", path, obj1, obj2))
            }
        } else if (obj1 != obj2) {
            differences.add(String.format("Path [%s] : values differs (%s != %s)", path, obj1, obj2))
        }
    }

    private fun compareMapsWithDiff(map1: MutableMap<String?, Any?>, map2: MutableMap<String?, Any?>, path: String, differences: MutableList<String?>) {
        val allKeys: MutableSet<String?> = HashSet()
        allKeys.addAll(map1.keys)
        allKeys.addAll(map2.keys)

        for (key in allKeys) {
            val currentPath: String = (if (path.isEmpty()) key else "$path.$key")!!

            if (!map1.containsKey(key)) {
                differences.add(String.format("path [%s] : missing key in expected file", currentPath))
            } else if (!map2.containsKey(key)) {
                differences.add(String.format("path [%s] : missing key in actual file", currentPath))
            } else {
                compareObjectsWithDiff(map1[key], map2[key], currentPath, differences)
            }
        }
    }

    private fun compareListsWithDiff(list1: MutableList<*>, list2: MutableList<*>, path: String?, differences: MutableList<String?>) {
        if (list1.size != list2.size) {
            differences.add(
              String.format(
                "Path [%s] : list size are different (expected file: %d != actual file: %d / [%s]/[%s])",
                path,
                list1.size,
                list2.size,
                list1.stream().map { obj: Any? -> obj.toString() }.collect(Collectors.joining(",")),
                list2.stream().map { obj: Any? -> obj.toString() }.collect(Collectors.joining(",")),
              )
            )
            return
        }

        val remainingItems: MutableList<*> = ArrayList(list2)

        for (i in list1.indices) {
            val item1: Any? = list1[i]
            var found = false

            for (j in remainingItems.indices) {
                val item2: Any? = remainingItems[j]
                if (compareObjects(item1, item2)) {
                    remainingItems.removeAt(j)
                    found = true
                    break
                }
            }

            if (!found) {
                differences.add(String.format("Path [%s][%d] : missing element in actual file", path, i))
            }
        }
    }
}
