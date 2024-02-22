plugins {
    id("java")
    id("maven-publish")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.deep_thinker"
            artifactId = "zeromq_client"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}

dependencies {
    implementation(project(":model"))
    implementation("org.zeromq:jeromq:0.5.4")
    implementation("com.google.flatbuffers:flatbuffers-java:23.5.26")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}