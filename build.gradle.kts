plugins {
    kotlin("jvm") version "1.9.0"

    application
}

group = "laz.dimboba"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Kord Snapshots Repository (Optional):
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    // Lavaplayer Repository
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    testImplementation(kotlin("test"))
    // html parser
    implementation("org.jsoup:jsoup:1.15.3")
    // Kord
    implementation("dev.kord:kord-core:0.10.0")
    implementation("dev.kord:kord-voice:0.10.0")
    implementation("dev.kord:kord-core-voice:0.10.0")
    // Lavaplayer
    implementation("com.sedmelluq:lavaplayer:1.3.77")
}

application {
    mainClass.set("laz.dimboba.dembobabot.MainKt")
}

tasks.test {
    useJUnitPlatform()
}



kotlin {
    jvmToolchain(8)
}