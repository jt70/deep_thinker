plugins {
    id("java")
}

group = "org.deep_thinker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val vertxVersion = "4.5.1"

dependencies {
    implementation(project(":zeromq_client"))
    implementation(project(":zeromq_server"))
    implementation(project(":model"))
    implementation(project(":agent"))
    implementation(project(":cartpole_environment"))
    implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
    implementation("io.vertx:vertx-core")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
