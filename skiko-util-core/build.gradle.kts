plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

dependencies {
    // apt
    ksp(project(":skiko-util-ksp"))
    implementation(project(":skiko-util-ksp"))
    // util
    api(project(":skiko-util-util"))
    api(project(":skiko-util-gif-codec"))
    api(project(":skiko-util-draw"))
    api(project(":skiko-util-bdf-parser"))
    // skiko
    compileOnly(skiko("windows-x64"))
    // serialization
    implementation(kotlinx("serialization-core-jvm", "1.5.0"))
    // kaml
    implementation("com.charleskorn.kaml:kaml:0.45.0")
//    // reflect
//    implementation(kotlin("reflect", Versions.kotlin))

    // test
    testImplementation(kotlin("test", Versions.kotlin))
    // skiko
    testImplementation(skiko("windows-x64"))
}

tasks {
    test {
        useJUnitPlatform()
        workingDir = rootProject.projectDir.resolve("run")
//        maxHeapSize = "8G"
//        minHeapSize = "8G"
    }
}
