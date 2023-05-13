import org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask
import java.io.File

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

dependencies {
    // apt
    kapt("top.e404:skiko-util-apt:0.0.1")
    implementation("top.e404:skiko-util-apt:0.0.1")
    // util
    api(project(":skiko-util-util"))
    api(project(":skiko-util-gif-codec"))
    api(project(":skiko-util-draw"))
    // skiko
    compileOnly(skiko("windows-x64"))
    compileOnly(skiko("linux-x64"))
    // serialization
    implementation(kotlinx("serialization-core-jvm", "1.3.3"))
    // kaml
    implementation("com.charleskorn.kaml:kaml:0.45.0")
    // reflect
    implementation(kotlin("reflect", Versions.kotlin))
    // test
    testImplementation(kotlin("test", Versions.kotlin))
    testImplementation(project(":skiko-util-util"))
}

tasks {
    test {
        useJUnit()
        workingDir = projectDir.resolve("run")
        maxHeapSize = "8G"
        minHeapSize = "8G"
    }

    withType<KaptWithoutKotlincTask>().configureEach {
        kaptProcessJvmArgs.add("-Xmx1G")
    }
}
