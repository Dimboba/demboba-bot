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
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
    // Kord Snapshots Repository (Optional):
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    // Lavaplayer Repository
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
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
    implementation("dev.arbjerg:lavaplayer:2.2.0")
    // Lavalink
//    implementation("dev.schlaubi.lavakord:core-jvm:7.0.3")
//    implementation("dev.schlaubi.lavakord:jda-jvm:5.1.7")
//    implementation("dev.schlaubi.lavakord:lavasearch-jvm:7.0.3")
// https://mvnrepository.com/artifact/dev.schlaubi.lavakord/kord
    implementation("dev.schlaubi.lavakord:kord:7.0.3")

//    runtimeOnly("dev.arbjerg.lavakord:kord-extensions:7.0.3")
//    implementation("com.github.kordlib:Lavalink.kt:7.0.2")
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
    jvmToolchain(17)
}

tasks.withType(ShadowJar::class.java) {
    archiveBaseName.set("demboba-bot")
 //   minimize()
}