plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":core"))
    // skiko
    runtimeOnly(skiko("windows-x64"))
    runtimeOnly(skiko("linux-x64"))
    // ktor
    implementation(ktor("server-content-negotiation"))
    implementation(ktor("server-compression-jvm"))
    implementation(ktor("serialization-kotlinx-json"))
    implementation(ktor("server-core-jvm"))
    implementation(ktor("server-netty-jvm"))
    implementation(ktor("server-call-logging-jvm"))
    // serialization
    implementation(kotlinx("serialization-core-jvm", Versions.KOTLINX_SERIALIZATION))
    implementation(kotlinx("serialization-json-jvm", Versions.KOTLINX_SERIALIZATION))
    // reflect
    implementation(kotlin("reflect", Versions.KOTLIN))
}

tasks {
    test {
        useJUnitPlatform()
        workingDir = rootProject.projectDir.resolve("run")
    }
}
