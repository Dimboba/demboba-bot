package laz.dimboba.dembobabot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.voice.AudioFrame
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//TODO: leave if there is no songs
//TODO: create query for songs (like !next)
class MusicPlayer (
    private val voiceConnectionsHandler: VoiceConnectionsHandler
) {
    private val lavaplayerManager = DefaultAudioPlayerManager()
    init {
        //lavaplayerManager.registerSourceManager(YoutubeAudioSourceManager())
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
    }

    private val player = lavaplayerManager.createPlayer()
    private val trackScheduler = TrackScheduler(player, voiceConnectionsHandler)
    init {
        player.addListener(trackScheduler)
    }

    suspend fun playYTSong(
        channel: BaseVoiceChannelBehavior,
        message: Message,
        searchString: String) {
        val search = searchString.trimStart()
        val query: String = if(search.startsWith("https://www.youtube.com") || search.startsWith("https://youtu.be")) {
            println("in if")
            if(search.contains("&")) {
                val suffix = search.subSequence(search.indexOf("&"), search.length - 1)
                search.removeSuffix(suffix)
            }
            search
        } else {
            "ytsearch: $search"
        }
        println(query)
        lavaplayerManager.loadTrack(query, message)
        trackScheduler.play(message, channel)
    }

    suspend fun stop(message: Message) {

    }

    suspend fun leave(message: Message) {
        voiceConnectionsHandler.closeConnections(message.getGuild().id)

        message.reply {
            content = "Music player is shutdown"
        }
    }


    private suspend fun DefaultAudioPlayerManager.loadTrack(query: String, message: Message) {
        val messageContent = suspendCoroutine<String> {
            this.loadItem(query, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    trackScheduler.queue(track)
                    it.resume("Add track: ${track.info.title}")
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    trackScheduler.queueList(playlist)
                    it.resume("Add ${playlist.tracks.size} tracks from ${playlist.name}")
                }


                override fun noMatches() {
                    it.resume("There is no such tracks")
                }

                override fun loadFailed(exception: FriendlyException?) {
                    it.resume("An error occurred")
                }
            })
        }

        message.reply {
            content = messageContent
        }
    }
}

