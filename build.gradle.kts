import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.0"

    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.3")
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

tasks.withType(ShadowJar::class.java) {
    archiveBaseName.set("demboba-bot")
 //   minimize()
}