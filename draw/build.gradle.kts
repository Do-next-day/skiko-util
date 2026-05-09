plugins {
    kotlin("jvm")
    jacoco
}

dependencies {
    // util
    api(project(":util"))
    // skiko
    api(skiko("windows-x64"))
    api(skiko("linux-x64"))
    // test
    testImplementation(kotlin("test", Versions.KOTLIN))
}

val manualTest by sourceSets.creating {
    java.srcDir("src/manualTest/kotlin")
    resources.srcDir("src/manualTest/resources")
    compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath
}

configurations["manualTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["manualTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

tasks.test {
    workingDir = rootDir.resolve("run")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }
}

tasks.register<Test>("manualTest") {
    description = "运行需要人工查看输出图片的 draw 测试"
    group = "verification"
    testClassesDirs = manualTest.output.classesDirs
    classpath = manualTest.runtimeClasspath
    workingDir = rootDir.resolve("run")
    shouldRunAfter(tasks.test)
}
