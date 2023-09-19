package laz.dimboba.dembobabot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
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
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//TODO: leave if there is no songs (occurs bugs)
//TODO: create query for songs (like !next)
//TODO: trackScheduler + MusicPlayer to one class

class MusicPlayer (
    private val voiceConnectionsHandler: VoiceConnectionsHandler
) {
    private val lavaplayerManager = DefaultAudioPlayerManager()
    init {
        //lavaplayerManager.registerSourceManager(YoutubeAudioSourceManager())
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
    }

    private var player: AudioPlayer? = null
    private var trackScheduler: TrackScheduler? = null

    suspend fun playYTSong(
        channel: BaseVoiceChannelBehavior,
        message: Message,
        searchString: String) {
        var isSearch = false
        val search = searchString.trimStart()
        val query: String = if(search.startsWith("https://www.youtube.com")
            || search.startsWith("https://youtu.be")
            || search.startsWith("https://youtube.com")) {
            val suffix = if(search.contains("&")) {
                search.subSequence(search.indexOf("&"), search.length)
            } else {
                ""
            }
            search.removeSuffix(suffix)
        } else {
            isSearch = true
            "ytsearch: $search"
        }
        if(player == null) {
            player = lavaplayerManager.createPlayer()
            trackScheduler = TrackScheduler(player!!, voiceConnectionsHandler)
            player?.addListener(trackScheduler)
        }
        lavaplayerManager.loadTrack(query, message, isSearch)
        trackScheduler?.play(message, channel)
    }

    fun pause(message: Message) {
        trackScheduler?.pause()
    }

    fun nextSong(message: Message) {
        trackScheduler?.nextSong(message)
    }

    suspend fun leave(message: Message) {
        voiceConnectionsHandler.closeConnections(message.getGuild().id)
        trackScheduler?.emptyQueue()
        player?.destroy()
        message.reply {
            content = "Music player is shutdown"
        }
    }


    private suspend fun DefaultAudioPlayerManager.loadTrack(
        query: String,
        message: Message,
        isSearch: Boolean = false) {
        val messageContent = suspendCoroutine<String> {
            this.loadItem(query, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    trackScheduler?.queue(track)
                    it.resume("Add track: ${track.info.title}")
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    if(isSearch){
                        trackScheduler?.queue(playlist.tracks.first())
                        it.resume("Add track: ${playlist.tracks.first().info.title}")
                        return
                    }
                    trackScheduler?.queueList(playlist)
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

