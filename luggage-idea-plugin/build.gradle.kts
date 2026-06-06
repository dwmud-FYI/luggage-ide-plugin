import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion")
        )

        bundledPlugins(
            providers.gradleProperty("platformBundledPlugins").map {
                it.split(",").map(String::trim).filter(String::isNotEmpty)
            }
        )

        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }   // open-ended — let users on newer IDEs install
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    publishing {
        token = providers.environmentVariable("JETBRAINS_MARKETPLACE_TOKEN")
            .orElse(providers.gradleProperty("publishToken"))
    }
}

kotlin {
    jvmToolchain(providers.gradleProperty("javaVersion").get().toInt())
}

// Copy shared schema + d.ts from the monorepo's shared/ folder into resources before packaging.
// Keeps single source of truth without check-in duplication.
val syncSharedAssets = tasks.register<Copy>("syncSharedAssets") {
    val sharedDir = rootProject.projectDir.parentFile.resolve("shared")

    from(sharedDir.resolve("schema")) {
        into("schemas")
    }
    from(sharedDir.resolve("types")) {
        into("types")
    }
    into(layout.projectDirectory.dir("src/main/resources"))
}

tasks.named("processResources") {
    dependsOn(syncSharedAssets)
}

tasks.named("compileKotlin") {
    dependsOn(syncSharedAssets)
}
