plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.3.2")
    // kaml
    implementation("com.charleskorn.kaml:kaml:0.43.0")
}
