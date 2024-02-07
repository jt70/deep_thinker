import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.deep_thinker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val vertxVersion = "4.5.1"
val junitJupiterVersion = "5.9.1"

dependencies {
    implementation(project(":model"))
    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-lang-kotlin")
    testImplementation("io.vertx:vertx-junit5")
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(platform("ai.djl:bom:0.25.0"))
    implementation("ai.djl:api")
    implementation("ai.djl:model-zoo")
    implementation("ai.djl.pytorch:pytorch-engine")
    implementation("ai.djl.pytorch:pytorch-model-zoo")
    implementation("org.msgpack:msgpack-core:0.9.8")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}