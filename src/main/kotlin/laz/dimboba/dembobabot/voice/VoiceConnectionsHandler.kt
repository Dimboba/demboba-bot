package laz.dimboba.dembobabot.voice

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.connect
import dev.kord.voice.VoiceConnection
import dev.kord.voice.VoiceConnectionBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Singleton

//TODO: check if channel is empty

private val logger = KotlinLogging.logger { }

@Singleton
class VoiceConnectionsHandler {

    init {
        logger.info {
            "VoiceConnectionHandler is started"
        }
    }

    @OptIn(KordVoice::class)
    private val connections: MutableMap<Snowflake, VoiceConnection> = mutableMapOf()

    @OptIn(KordVoice::class)
    suspend fun closeConnections(id: Snowflake) {
        if (connections.contains(id)) {
            connections.remove(id)!!.shutdown()
        }
    }

    @OptIn(KordVoice::class)
    suspend fun connect(
        channelBehavior: BaseVoiceChannelBehavior,
        id: Snowflake,
        builder: VoiceConnectionBuilder.() -> Unit
    ): VoiceConnection {
        val connection = channelBehavior.connect(builder)
        connections[id] = connection
        return connection
    }

    @OptIn(KordVoice::class)
    fun isConnected(
        id: Snowflake
    ) = connections.keys.contains(id)
}