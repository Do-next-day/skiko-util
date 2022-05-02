plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.jetbrains.kotlin.kapt") version "1.6.20"
    `maven-publish`
    `java-library`
}

group = "top.e404"
version = "1.0.0"

val skikoVer = "0.7.16"
fun skiko(module: String) = "org.jetbrains.skiko:skiko-awt-runtime-$module:$skikoVer"

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        // kotlin
        implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.20")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.1-native-mt")
    }
}

dependencies {
    // apt
    kapt("top.e404:skiko-util-apt:0.0.1")
    implementation("top.e404:skiko-util-apt:0.0.1")
    // skiko
    api(skiko("windows-x64"))
    api(skiko("linux-x64"))
    // reflect
    implementation(kotlin("reflect", "1.6.20"))
    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.3.2")
    // kaml
    implementation("com.charleskorn.kaml:kaml:0.43.0")
    // test
    testImplementation(kotlin("test", "1.6.20-M1"))
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
        groupId = "top.e404"
        artifactId = "skiko-util"
        version = "1.0.0"
    }
}

tasks.test {
    useJUnit()
    workingDir = projectDir.resolve("run")
}