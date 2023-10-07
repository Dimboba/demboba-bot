import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.10"

    application
    //ShadowJar
    id("com.github.johnrengelman.shadow") version "8.1.1"
    //DI
    id("com.google.devtools.ksp") version "1.9.10-1.0.13"
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
    // Koin
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("io.insert-koin:koin-annotations:1.3.0")
    ksp("io.insert-koin:koin-ksp-compiler:1.3.0")

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