plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":model"))
    implementation("org.zeromq:jeromq:0.5.4")
    implementation("org.msgpack:msgpack-core:0.9.8")
    implementation("com.google.flatbuffers:flatbuffers-java:23.5.26")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}