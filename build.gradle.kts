import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("idea")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.5.0"
    id("org.jetbrains.changelog") version "2.2.1"
    id("org.jetbrains.qodana") version "2024.3.4"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    id("com.diffplug.spotless") version "7.2.1"
    id("org.owasp.dependencycheck") version "12.1.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"
}

group = providers.gradleProperty("pluginGroup").get()

version = providers.gradleProperty("pluginVersion").get()

kotlin { jvmToolchain(21) }

repositories {
    mavenCentral()
    maven { url = uri("https://repo1.maven.org/maven2/") }

    intellijPlatform { defaultRepositories() }
}

dependencyCheck { suppressionFile = "suppression.xml" }

dependencies {
    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")
    implementation("com.knuddels:jtokkit:1.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.github.victools:jsonschema-generator:[4.21.0,5.0.0)")
    implementation("com.github.victools:jsonschema-module-jackson:[4.21.0,5.0.0)")
    implementation("io.swagger.parser.v3:swagger-parser-v3:2.1.33")
    implementation("com.github.javaparser:javaparser-core:3.25.5")
    implementation("io.github.classgraph:classgraph:4.8.158")
    implementation("com.opencsv:opencsv:5.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("jaxen:jaxen:2.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    testImplementation("org.assertj:assertj-core:3.11.1")

    intellijPlatform {
        create(
            providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        pluginVerifier()

        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
    }

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the
        // plugin's manifest
        description =
            providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                with(it.lines()) {
                    if (!containsAll(listOf(start, end))) {
                        throw GradleException(
                            "Plugin description section not found in README.md:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end))
                        .joinToString("\n")
                        .let(::markdownToHTML)
                }
            }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes =
            providers.gradleProperty("pluginVersion").map { pluginVersion ->
                with(changelog) {
                    renderItem(
                        (getOrNull(pluginVersion) ?: getUnreleased())
                            .withHeader(false)
                            .withEmptySections(false),
                        Changelog.OutputType.HTML,
                    )
                }
            }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels =
            providers.gradleProperty("pluginVersion").map {
                listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
            }
    }

    pluginVerification { ides { ide(IntelliJPlatformType.PhpStorm, "2025.1") } }
}

// Configure Gradle Changelog Plugin - read more:
// https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

kover { reports { total { xml { onCheck = true } } } }

tasks {
    test {
        // Support "setUp" like "BasePlatformTestCase::setUp" as valid test structure
        useJUnitPlatform { includeEngines("junit-vintage") }
    }

    wrapper { gradleVersion = providers.gradleProperty("gradleVersion").get() }

    publishPlugin { dependsOn(patchChangelog) }
}

intellijPlatformTesting {
    runIde {
        register("runIdeForUiTests") {
            task {
                jvmArgumentProviders += CommandLineArgumentProvider {
                    listOf(
                        "-Drobot-server.port=8082",
                        "-Dide.mac.message.dialogs.as.sheets=false",
                        "-Djb.privacy.policy.text=<!--999.999-->",
                        "-Djb.consents.confirmation.enabled=false",
                    )
                }
            }

            plugins { robotServerPlugin() }
        }
    }
}

spotless {
    isEnforceCheck = false

    java {
        target("src/**/*.java")
        formatAnnotations()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        toggleOffOn()
    }

    kotlin {
        target("**/*.kt", "**/*.kts")
        ktfmt("0.51").kotlinlangStyle()
    }

    format("misc") {
        target("**/*.md", "**/*.yml", "**/*.yaml", "**/*.xml")
        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
    }
}
