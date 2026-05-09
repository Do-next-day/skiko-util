import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import kotlin.apply

plugins {
    kotlin("jvm") version Versions.KOTLIN
    kotlin("plugin.serialization") version Versions.KOTLIN
    `maven-publish`
    `java-library`
    idea
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = Versions.GROUP
    version = Versions.VERSION

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        // kotlin
        implementation(kotlinx("coroutines-core-jvm", "1.10.2"))
    }
}

val local = Properties().apply {
    val file = projectDir.resolve("local.properties")
    if (file.exists()) file.bufferedReader().use { load(it) }
}

val nexusUsername get() = local.getProperty("nexus.username") ?: ""
val nexusPassword get() = local.getProperty("nexus.password") ?: ""

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.gradle.java-library")

    kotlin {
        jvmToolchain(11)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    java {
        withSourcesJar()
    }

    afterEvaluate {
        publishing.publications.create<MavenPublication>("java") {
            from(components["kotlin"])
            artifact(tasks.getByName("sourcesJar"))
            artifactId = "${rootProject.name}-${project.name}"
            groupId = Versions.GROUP
            version = Versions.VERSION
        }
    }

    publishing {
        repositories {
            maven {
                name = "snapshot"
                url = uri("https://nexus.e404.top:3443/repository/maven-snapshots/")
                isAllowInsecureProtocol = true
                credentials {
                    username = nexusUsername
                    password = nexusPassword
                }
            }
        }
    }
}

tasks.assemble {
    subprojects.forEach {
        dependsOn(it.tasks.assemble)
    }
}

idea {
    module.excludeDirs.addAll(arrayOf(
        file("run"),
        file(".idea"),
        file(".kotlin"),
    ))
}