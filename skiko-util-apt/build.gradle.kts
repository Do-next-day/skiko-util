plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    `java-library`
}

dependencies {
    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.3.2")
    // kaml
    implementation("com.charleskorn.kaml:kaml:0.43.0")
}

java {
    withJavadocJar()
    withSourcesJar()
}

afterEvaluate {
    publishing.publications.create<MavenPublication>("java") {
        from(components["kotlin"])
        artifact(tasks.getByName("sourcesJar"))
        artifact(tasks.getByName("javadocJar"))
        groupId = "top.e404"
        artifactId = "skiko-util-apt"
        version = "0.0.1"
    }
}