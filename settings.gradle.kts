pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "1.8.21-1.0.11"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "skiko-util"
include(
    ":skiko-util-ksp",
    ":skiko-util-core",
    ":skiko-util-gif-codec",
    ":skiko-util-draw",
    ":skiko-util-util",
    ":skiko-util-bdf-parser",
)
