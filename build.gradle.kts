import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import java.time.Year
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("org.jetbrains.intellij") version "1.16.0"
    id("org.jetbrains.changelog") version "2.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

group = project.findProperty("pluginGroup").toString()
version = project.findProperty("pluginVersion").toString()

repositories {
    mavenCentral()
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
    testImplementation("junit:junit:4.13.2")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
//    config.setFrom(files("$projectDir/config/detekt.yml"))
//    baseline = file("$projectDir/config/baseline.xml")
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
    jvmTarget = "17"
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "17"
}

intellij {
    version.set("2024.1") // Use the version that matches your IDE
    type.set("IU") // Use "IC" for IntelliJ IDEA Community Edition
    plugins.set(listOf("java"))
//    pluginName.set(project.findProperty("pluginName").toString())
//    version.set(project.findProperty("platformVersion").toString())
//    type.set(project.findProperty("platformType").toString())
//    downloadSources.set(project.findProperty("platformDownloadSources").toString().toBoolean())
//    updateSinceUntilBuild.set(true)
//    plugins.set(
//        project.findProperty("platformPlugins").toString().split(',').map(String::trim).filter(String::isNotEmpty)
//    )
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(project.version.toString())
    path.set(file("CHANGELOG.md").canonicalPath)
    header.set(provider { "[${project.version}] - ${Year.now()}" })
    headerParserRegex.set("""(\d+\.\d+)""".toRegex())
    introduction.set(
        """
        My awesome project
        """.trimIndent()
    )
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
}

tasks {
    patchPluginXml {
        version.set(project.version.toString())
        sinceBuild.set(project.findProperty("pluginSinceBuild").toString())
        untilBuild.set(project.findProperty("pluginUntilBuild").toString())

        pluginDescription.set(
            File(projectDir, "README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        changeNotes.set(provider {
            changelog.renderItem(
                changelog.getUnreleased()
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML
            )
        })

    }

    runPluginVerifier {
        ideVersions.set(
            project.findProperty("pluginVerifierIdeVersions").toString().split(',').map(String::trim)
                .filter(String::isNotEmpty)
        )
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    test {
        useJUnitPlatform()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}