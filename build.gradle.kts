plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "top.e404"
version = "0.0.1"

repositories {
    mavenCentral()
}

val skikoVer = "0.7.16"
fun skiko(module: String) = "org.jetbrains.skiko:skiko-awt-runtime-$module:$skikoVer"

dependencies {
    // kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.0")
    // skiko
    api(skiko("windows-x64"))
    api(skiko("macos-x64"))
    api(skiko("macos-arm64"))
    api(skiko("linux-x64"))
    api(skiko("linux-arm64"))
    // test
    testImplementation(kotlin("test", "1.6.20-M1"))
}