plugins {
    kotlin("jvm") version "1.9.0"
}

group = "laz.dimboba"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Kord Snapshots Repository (Optional):
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.kord:kord-core:0.10.0")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("dev.kord:kord-voice:0.10.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}