package org.micoli.php.configuration.documentation;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class JavaDocumentationGenerator {

    public static class ClassInfo {
        private String name;
        private String description;
        private List<MethodInfo> methods = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<MethodInfo> getMethods() {
            return methods;
        }
    }

    public static class MethodInfo {
        private String name;
        private String signature;
        private String description;
        private List<ParameterInfo> parameters = new ArrayList<>();
        private String returnDescription;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<ParameterInfo> getParameters() {
            return parameters;
        }

        public String getReturnDescription() {
            return returnDescription;
        }

        public void setReturnDescription(String returnDescription) {
            this.returnDescription = returnDescription;
        }
    }

    public static class ParameterInfo {
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static Map<String, JavaDocumentationGenerator.ClassInfo> extractJavadocs(String sourcePath) {
        Map<String, JavaDocumentationGenerator.ClassInfo> classInfos = new HashMap<>();

        try (Stream<Path> paths = Files.walk(Paths.get(sourcePath))) {
            List<Path> javaFiles = paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();

            for (Path javaFile : javaFiles) {
                try (FileInputStream in = new FileInputStream(javaFile.toFile())) {
                    ParseResult<CompilationUnit> parseResult = new JavaParser().parse(in);
                    if (parseResult.getResult().isEmpty()) {
                        continue;
                    }
                    CompilationUnit cu = parseResult.getResult().get();
                    for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                        if (classDecl.getNameAsString().contains("Error")
                                || classDecl.getNameAsString().contains("Exception")) {
                            continue;
                        }
                        JavaDocumentationGenerator.ClassInfo classInfo = new JavaDocumentationGenerator.ClassInfo();
                        classInfo.setName(classDecl.getNameAsString());

                        Optional<Javadoc> classJavadoc = classDecl.getJavadoc();
                        classJavadoc.ifPresent(javadoc -> classInfo.setDescription(
                                javadoc.getDescription().toText()));

                        for (MethodDeclaration methodDecl : classDecl.getMethods()) {
                            if (methodDecl.isPublic()) {
                                JavaDocumentationGenerator.MethodInfo methodInfo =
                                        new JavaDocumentationGenerator.MethodInfo();
                                methodInfo.setName(methodDecl.getNameAsString());
                                methodInfo.setSignature(methodDecl.getDeclarationAsString(false, false, true));

                                Optional<com.github.javaparser.javadoc.Javadoc> methodJavadoc = methodDecl.getJavadoc();
                                if (methodJavadoc.isPresent()) {
                                    methodInfo.setDescription(
                                            methodJavadoc.get().getDescription().toText());

                                    methodJavadoc.get().getBlockTags().stream()
                                            .filter(tag -> tag.getType()
                                                    == com.github.javaparser.javadoc.JavadocBlockTag.Type.PARAM)
                                            .forEach(tag -> {
                                                JavaDocumentationGenerator.ParameterInfo paramInfo =
                                                        new JavaDocumentationGenerator.ParameterInfo();
                                                paramInfo.setName(tag.getName().orElse(""));
                                                paramInfo.setDescription(
                                                        tag.getContent().toText());
                                                methodInfo.getParameters().add(paramInfo);
                                            });

                                    methodJavadoc.get().getBlockTags().stream()
                                            .filter(tag -> tag.getType()
                                                    == com.github.javaparser.javadoc.JavadocBlockTag.Type.RETURN)
                                            .findFirst()
                                            .ifPresent(tag -> methodInfo.setReturnDescription(
                                                    tag.getContent().toText()));
                                }

                                classInfo.getMethods().add(methodInfo);
                            }
                        }

                        classInfos.put(classInfo.getName(), classInfo);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return classInfos;
    }

    public static String generateMarkdownDocumentation(String sourcePath) {
        Map<String, ClassInfo> classInfos = JavaDocumentationGenerator.extractJavadocs(sourcePath);
        StringBuilder markdown = new StringBuilder();
        for (ClassInfo classInfo : classInfos.values()) {
            markdown.append("#### ")
                    .append("`")
                    .append(classInfo.getName())
                    .append("`")
                    .append("\n\n");
            if (classInfo.getDescription() != null) {
                markdown.append(classInfo.getDescription()).append("\n\n");
            }

            if (!classInfo.getMethods().isEmpty()) {
                for (MethodInfo methodInfo : classInfo.getMethods()) {
                    markdown.append("- `").append(methodInfo.getSignature()).append("`\n");
                    if (methodInfo.getDescription() != null) {
                        markdown.append("  ")
                                .append(methodInfo.getDescription().replaceAll("\n", " "))
                                .append("\n");
                    }

                    if (!methodInfo.getParameters().isEmpty()) {
                        for (ParameterInfo paramInfo : methodInfo.getParameters()) {
                            markdown.append("   - `")
                                    .append(paramInfo.getName())
                                    .append("`: ")
                                    .append(paramInfo.getDescription().replaceAll("\n", " "))
                                    .append("\n");
                        }
                        markdown.append("\n");
                    }

                    if (methodInfo.getReturnDescription() != null) {
                        markdown.append(methodInfo.getReturnDescription()).append("\n\n");
                    }
                }
            }
        }

        return markdown.toString();
    }
}
