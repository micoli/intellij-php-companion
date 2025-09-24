package org.micoli.php.configuration.documentation

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.javadoc.Javadoc
import com.github.javaparser.javadoc.JavadocBlockTag
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Consumer

object JavaDocumentationGenerator {
    fun extractJavadocs(sourcePath: String): MutableMap<String?, ClassInfo>? {
        val classInfos: MutableMap<String?, ClassInfo> = HashMap()

        try {
            Files.walk(Paths.get(sourcePath)).use { paths ->
                val javaFiles =
                    paths
                        .filter { path: Path? -> path != null }
                        .filter { path: Path -> Files.isRegularFile(path) }
                        .filter { path: Path -> path.toString().endsWith(".java") }
                        .toList()
                for (javaFile in javaFiles) {
                    try {
                        FileInputStream(javaFile.toFile()).use { `in` ->
                            val parseResult = JavaParser().parse(`in`)
                            if (parseResult.getResult().isEmpty) {
                                return null
                            }
                            val cu = parseResult.getResult().get()
                            for (classDecl in cu.findAll(ClassOrInterfaceDeclaration::class.java)) {
                                if (classDecl.nameAsString.contains("Error") ||
                                    classDecl.nameAsString.contains("Exception")) {
                                    continue
                                }
                                val classInfo = ClassInfo()
                                classInfo.name = classDecl.nameAsString

                                val classJavadoc = classDecl.javadoc
                                classJavadoc.ifPresent(
                                    Consumer { javadoc: Javadoc? ->
                                        classInfo.description = javadoc!!.description.toText()
                                    })

                                for (methodDecl in classDecl.methods) {
                                    if (methodDecl.isPublic) {
                                        val methodInfo = MethodInfo()
                                        methodInfo.name = methodDecl.nameAsString
                                        methodInfo.signature =
                                            methodDecl.getDeclarationAsString(false, false, true)

                                        val methodJavadoc = methodDecl.javadoc
                                        if (methodJavadoc.isPresent) {
                                            methodInfo.description =
                                                methodJavadoc.get().description.toText()

                                            methodJavadoc
                                                .get()
                                                .blockTags
                                                .stream()
                                                .filter { tag: JavadocBlockTag? ->
                                                    (tag!!.type == JavadocBlockTag.Type.PARAM)
                                                }
                                                .forEach { tag: JavadocBlockTag? ->
                                                    val paramInfo = ParameterInfo()
                                                    paramInfo.name = tag!!.name.orElse("")
                                                    paramInfo.description = tag.content.toText()
                                                    methodInfo.parameters.add(paramInfo)
                                                }

                                            methodJavadoc
                                                .get()
                                                .blockTags
                                                .stream()
                                                .filter { tag: JavadocBlockTag? ->
                                                    (tag!!.type == JavadocBlockTag.Type.RETURN)
                                                }
                                                .findFirst()
                                                .ifPresent(
                                                    Consumer { tag: JavadocBlockTag? ->
                                                        methodInfo.returnDescription =
                                                            tag!!.content.toText()
                                                    })
                                        }

                                        classInfo.methods.add(methodInfo)
                                    }
                                }

                                classInfos[classInfo.name] = classInfo
                            }
                        }
                    } catch (e: Exception) {
                        throw RuntimeException(e.message)
                    }
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e.message)
        }

        return classInfos
    }

    fun generateMarkdownDocumentation(sourcePath: String): String {
        val classInfos = extractJavadocs(sourcePath) ?: return ""
        val markdown = StringBuilder()
        for (classInfo in classInfos.values) {
            markdown.append("#### ").append("`").append(classInfo.name).append("`").append("\n\n")
            if (classInfo.description != null) {
                markdown.append(classInfo.description).append("\n\n")
            }

            if (!classInfo.methods.isEmpty()) {
                for (methodInfo in classInfo.methods) {
                    markdown.append("- `").append(methodInfo.signature).append("`\n")
                    if (methodInfo.description != null) {
                        markdown
                            .append("  ")
                            .append(methodInfo.description!!.replace("\n".toRegex(), " "))
                            .append("\n")
                    }

                    if (!methodInfo.parameters.isEmpty()) {
                        for (paramInfo in methodInfo.parameters) {
                            markdown
                                .append("   - `")
                                .append(paramInfo.name)
                                .append("`: ")
                                .append(paramInfo.description!!.replace("\n".toRegex(), " "))
                                .append("\n")
                        }
                        markdown.append("\n")
                    }

                    if (methodInfo.returnDescription != null) {
                        markdown.append(methodInfo.returnDescription).append("\n\n")
                    }
                }
            }
        }

        return markdown.toString()
    }

    class ClassInfo {
        var name: String? = null
        var description: String? = null
        val methods: MutableList<MethodInfo> = ArrayList()
    }

    class MethodInfo {
        var name: String? = null
        var signature: String? = null
        var description: String? = null
        val parameters: MutableList<ParameterInfo> = ArrayList()
        var returnDescription: String? = null
    }

    class ParameterInfo {
        var name: String? = null
        var description: String? = null
    }
}
