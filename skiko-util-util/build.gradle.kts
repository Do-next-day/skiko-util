plugins {
    kotlin("jvm")
}

dependencies {
    // skiko
    compileOnly(skiko("windows-x64"))
    compileOnly(skiko("linux-x64"))
}
