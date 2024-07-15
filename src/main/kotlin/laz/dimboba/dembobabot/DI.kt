package laz.dimboba.dembobabot

import dev.kord.core.Kord
import dev.schlaubi.lavakord.kord.lavakord
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

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
}
