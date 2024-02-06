plugins {
    kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

val vertxVersion = "4.5.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":agent"))
    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-lang-kotlin")
    testImplementation("io.vertx:vertx-junit5")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.zeromq:jeromq:0.5.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}