package laz.dimboba.dembobabot

import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import laz.dimboba.dembobabot.channel.ChannelHandler
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
@ComponentScan("laz.dimboba.dembobabot.voice")
class VoiceModel {
}

@Module
@ComponentScan("laz.dimboba.dembobabot.overwatch")
class OverwatchModel {
}

@Module
@ComponentScan("laz.dimboba.dembobabot.controller")
class ControllerModel {
}

@Module
@ComponentScan("laz.dimboba.dembobabot.channel")
class ChannelModel {
    @Singleton
    @Named("MusicTextChannel")
    fun musicTextChannel(channelHandler: ChannelHandler) = runBlocking {
        channelHandler.getTextMessageChannelInstance("demboba-dj")
    }
}

@Module
class MainModel {
    @Singleton
    @Named("ServerGuild")
    fun serverGuild() = runBlocking {
        kord!!.guilds.first().also { println("hello") }
    }
}
