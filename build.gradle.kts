plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("kapt") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    `maven-publish`
    `java-library`
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = Versions.group
    version = Versions.version

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        // kotlin
        implementation(kotlinx("coroutines-core-jvm", "1.6.4"))
    }
}

subprojects {
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.gradle.java-library")

    java {
        withSourcesJar()
    }

    afterEvaluate {
        publishing.publications.create<MavenPublication>("java") {
            artifact(tasks.jar)
            artifact(tasks.getByName("sourcesJar"))
            artifactId = project.name
            groupId = project.group.toString()
            version = project.version.toString()
        }
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
    }
}

tasks {
    create("skiko-util-publish") {
        group = "skiko-util"
        dependsOn(subprojects.map { it.tasks.publishToMavenLocal })
    }

    create("skiko-util-clean") {
        group = "skiko-util"
        dependsOn(subprojects.map { it.tasks.clean })
    }

    create("skiko-util-assemble") {
        group = "skiko-util"
        dependsOn(subprojects.map { it.tasks.assemble })
    }
}
