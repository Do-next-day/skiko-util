plugins {
    kotlin("jvm")
}

dependencies {
    // util
    api(project(":skiko-util-util"))
    api(project(":skiko-util-gif-codec"))
    // skiko
    api(skiko("windows-x64"))
    api(skiko("linux-x64"))
}
