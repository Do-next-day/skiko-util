plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("kapt") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    `maven-publish`
    `java-library`
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        // kotlin
        implementation(kotlinx("coroutines-core-jvm", "1.6.2"))
    }
}

dependencies {
    // apt
    kapt("top.e404:skiko-util-apt:0.0.1")
    implementation("top.e404:skiko-util-apt:0.0.1")
    // skiko
    api(skiko("windows-x64"))
    api(skiko("linux-x64"))
    // serialization
    implementation(kotlinx("serialization-core-jvm", "1.3.3"))
    // kaml
    implementation("com.charleskorn.kaml:kaml:0.45.0")
    // reflect
    implementation(kotlin("reflect", Versions.kotlin))
    // test
    testImplementation(kotlin("test", Versions.kotlin))
}

java {
    withJavadocJar()
    withSourcesJar()
}

afterEvaluate {
    publishing.publications.create<MavenPublication>("java") {
        from(components["kotlin"])
        artifact(tasks.getByName("sourcesJar"))
        artifact(tasks.getByName("javadocJar"))
        artifactId = "skiko-util"
        groupId = project.group.toString()
        version = project.version.toString()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask>()
    .configureEach { kaptProcessJvmArgs.add("-Xmx1G") }

tasks.jar {
    doLast {
        println("==== copy ====")
        for (file in File("build/libs").listFiles() ?: emptyArray()) {
            if ("source" in file.name || "javadoc" in file.name) continue
            println("正在复制`${file.path}`")
            file.copyTo(File("jar/${file.name}"), true)
        }
    }
}

tasks.test {
    useJUnit()
    workingDir = projectDir.resolve("run")
    maxHeapSize = "8G"
    minHeapSize = "8G"
}