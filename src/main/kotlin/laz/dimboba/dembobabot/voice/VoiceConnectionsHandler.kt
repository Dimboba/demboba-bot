package laz.dimboba.dembobabot.voice

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.connect
import dev.kord.voice.VoiceConnection
import dev.kord.voice.VoiceConnectionBuilder

//TODO: check if channel is empty

class VoiceConnectionsHandler {
    @OptIn(KordVoice::class)
    private val connections: MutableMap<Snowflake, VoiceConnection> = mutableMapOf()

    @OptIn(KordVoice::class)
    suspend fun closeConnections(guildId: Snowflake){
        if (connections.contains(guildId)) {
            connections.remove(guildId)!!.shutdown()
        }
    }

    @OptIn(KordVoice::class)
    suspend fun connect(
        channelBehavior: BaseVoiceChannelBehavior,
        guildId: Snowflake,
        builder: VoiceConnectionBuilder.() -> Unit
    ): VoiceConnection {
        val connection = channelBehavior.connect(builder)
        connections[guildId] = connection
        return connection
    }
}