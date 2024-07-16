package laz.dimboba.dembobabot

import dev.kord.core.Kord
import dev.schlaubi.lavakord.kord.lavakord
import io.github.classgraph.ClassGraph
import kotlinx.coroutines.runBlocking
import laz.dimboba.dembobabot.controller.CommandAction
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import org.koin.core.qualifier.named
import java.lang.reflect.Method

@Module
@ComponentScan("laz.dimboba.dembobabot.voice")
class VoiceModule {

    @Singleton(createdAtStart = true)
    fun lavaKord(kord: Kord) = runBlocking {
        val lavakord = kord.lavakord()
        lavakord.addNode("ws://localhost:2333", "youshallnotpass")
        return@runBlocking lavakord

    }
}

@Module
@ComponentScan("laz.dimboba.dembobabot.overwatch")
class OverwatchModule

@Module
@ComponentScan("laz.dimboba.dembobabot.controller")
class ControllerModule

@Module
@ComponentScan("laz.dimboba.dembobabot.channel")
class ChannelModule

@Module
class MainModule {
    @Singleton
    fun kord(): Kord = runBlocking {
        val token: String = System.getenv("discord_token") ?: "null-token"
        return@runBlocking Kord(token)
    }
    @Singleton(createdAtStart = true)
    @Named("commandMethods")
    fun commandMethods(): List<Method> {
        val annotation = CommandAction::class.java
        return ClassGraph()
            .ignoreClassVisibility()
            .enableAllInfo()
            .acceptPackages("laz.dimboba.dembobabot")
            .scan(). use { scanResult ->
                scanResult.getClassesWithMethodAnnotation(annotation)
                    .flatMap { it.loadClass().declaredMethods.toList() }
                    .filter { it.getAnnotation(annotation) != null }
            }
    }
}

@Module
@ComponentScan("laz.dimboba.dembobabot.help")
class HelpModule
