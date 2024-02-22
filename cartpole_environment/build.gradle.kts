plugins {
    id("java")
    id("maven-publish")
}

group = "cartpole_environment"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.deep_thinker"
            artifactId = "cartpole_environment"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":model"))
}

tasks.test {
    useJUnitPlatform()
}