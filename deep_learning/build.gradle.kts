plugins {
    kotlin("jvm")
}

group = "org.deep_thinker.nn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.slf4j:slf4j-simple:2.0.12")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}