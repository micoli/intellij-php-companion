package org.micoli.php.configuration.schema.valueGenerator

import java.io.IOException
import java.net.URISyntaxException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.jar.JarFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class IconValueGenerator : PropertyValueGenerator {
    override fun getFieldNames(): ImmutableList<String> = persistentListOf("icon", "activeIcon", "inactiveIcon", "unknownIcon")

    override fun getValues(): ImmutableList<String> {
        val expUIPath = "expui"
        val resources: MutableList<String> = ArrayList<String>()
        try {
            val url = javaClass.getClassLoader().getResource(expUIPath) ?: return resources.toImmutableList()

            val protocol = url.protocol
            when (protocol) {
                "file" -> {
                    val basePath = Paths.get(url.toURI())
                    Files.walkFileTree(
                      basePath,
                      object : SimpleFileVisitor<Path>() {
                          override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                              val filePath = file.toString()
                              if (!attrs.isDirectory && filePath.startsWith("$expUIPath/") && filePath.endsWith(".svg")) {
                                  resources.add(filePath)
                              }
                              return FileVisitResult.CONTINUE
                          }
                      },
                    )
                }
                "jar" -> {
                    val jarPath = url.path.substring(expUIPath.length, url.path.indexOf("!"))
                    try {
                        JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8)).use { jar ->
                            val entries = jar.entries()
                            while (entries.hasMoreElements()) {
                                val entry = entries.nextElement()
                                val filePath = entry.getName()
                                if (!entry.isDirectory && filePath.startsWith("$expUIPath/") && filePath.endsWith(".svg")) {
                                    resources.add(filePath)
                                }
                            }
                        }
                    } catch (_: IOException) {}
                }
            }
        } catch (_: URISyntaxException) {} catch (_: IOException) {}

        return resources.stream().filter { obj: String? -> Objects.nonNull(obj) }.distinct().sorted().toList().toImmutableList()
    }
}
