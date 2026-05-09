plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

dependencies {
    // ksp
    ksp(project(":ksp"))

    api(project(":annotation"))
    api(project(":util"))
    api(project(":gif-codec"))
    api(project(":draw"))
    api(project(":bdf-parser"))
    // skiko
    compileOnly(skiko("windows-x64"))
    // serialization
    implementation(kotlinx("serialization-core-jvm", Versions.KOTLINX_SERIALIZATION))
    // kaml
    implementation("com.charleskorn.kaml:kaml:0.45.0")
//    // reflect
//    implementation(kotlin("reflect", Versions.kotlin))

    // test
    testImplementation(kotlin("test", Versions.KOTLIN))
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
