package laz.dimboba.dembobabot

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MusicPlayer (
    private val voiceConnectionsHandler: VoiceConnectionsHandler
) {
    private val lavaplayerManager = DefaultAudioPlayerManager()

    private val player = lavaplayerManager.createPlayer()

    init {
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
    }

    @OptIn(KordVoice::class)
    suspend fun playYTSong(
        channel: BaseVoiceChannelBehavior,
        message: Message,
        searchString: String): String {

        val query = "ytsearch: $searchString"

        val track = lavaplayerManager.playTrack(query, player)

        voiceConnectionsHandler.closeConnections(message.getGuild().id)
        voiceConnectionsHandler.connect(
            channelBehavior = channel,
            guildId = message.getGuild().id
        ) {
            audioProvider {
                AudioFrame.fromData(player.provide().data)
            }
        }

        return track.info.title
    }

    suspend fun stopSong(message: Message) {
        voiceConnectionsHandler.closeConnections(message.getGuild().id)

    }


    private suspend fun DefaultAudioPlayerManager.playTrack(query: String, player: AudioPlayer): AudioTrack {
        val track = suspendCoroutine<AudioTrack> {
            this.loadItem(query, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    it.resume(track)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    it.resume(playlist.tracks.first())
                }

                override fun noMatches() {
                    //TODO()
                }

                override fun loadFailed(exception: FriendlyException?) {
                    //TODO()
                }
            })
        }

        player.playTrack(track)

        return track
    }
}

