plugins {
    kotlin("jvm")
}

group = "org.deep_thinker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.msgpack:msgpack-core:0.9.8")
    implementation("com.google.flatbuffers:flatbuffers-java:23.5.26")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}