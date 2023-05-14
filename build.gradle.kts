import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("kapt") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = Versions.group
    version = Versions.version

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        // kotlin
        implementation(kotlinx("coroutines-core-jvm", "1.6.4"))
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "me.him188.maven-central-publish")

    kotlin {
        jvmToolchain(11)
    }

    mavenCentralPublish {
        useCentralS01()
        singleDevGithubProject("4o4E", "skiko-util")
        licenseGplV3()
        workingDir = buildDir.resolve("publishing-tmp")
    }

    tasks {
        assemble {
            doLast {
                val jar = rootProject.projectDir.resolve("jar")
                jar.mkdir()
                println("==== copy ====")
                for (file in project.buildDir.resolve("libs").listFiles() ?: emptyArray()) {
                    if ("source" in file.name) continue
                    println("正在复制`${file.path}`")
                    file.copyTo(jar.resolve(file.name), true)
                }
            }
        }

        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "11"
        }
    }
}
