plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    // util
    api(project(":util"))
    // skiko
    compileOnly(skiko("windows-x64"))
    compileOnly(skiko("linux-x64"))
}
