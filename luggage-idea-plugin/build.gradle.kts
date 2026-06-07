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
        intellijDependencies()   // hosts javac2 / forms-rt for the instrumentCode task
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

        instrumentationTools()
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

// Pull shared schema + d.ts from the monorepo's shared/ folder into a generated
// resource directory. Hooked into the source set so Gradle wires processResources /
// patchPluginXml / compileKotlin to depend on it automatically — no implicit-dep
// warnings, no checked-in copies of the shared assets.
val syncSharedAssets = tasks.register<Sync>("syncSharedAssets") {
    val sharedDir = rootProject.projectDir.parentFile.resolve("shared")

    from(sharedDir.resolve("schema")) { into("schemas") }
    from(sharedDir.resolve("types"))  { into("types") }

    into(layout.buildDirectory.dir("generated/sharedAssets"))
}

sourceSets["main"].resources.srcDir(syncSharedAssets)
