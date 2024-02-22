plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "org.deep_thinker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.deep_thinker"
            artifactId = "model"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}

dependencies {
    implementation("com.google.flatbuffers:flatbuffers-java:23.5.26")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}