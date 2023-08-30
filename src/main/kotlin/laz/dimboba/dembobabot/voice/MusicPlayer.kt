package laz.dimboba.dembobabot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.voice.AudioFrame
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//TODO: leave if there is no songs
//TODO: create query for songs (like !next)
//TODO: url for songs (kill channel url at the end)
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

    @OptIn(KordVoice::class)
    suspend fun playYTSong(
        channel: BaseVoiceChannelBehavior,
        message: Message,
        searchString: String): String {

        if(searchString.contains("www.youtube.com") || searchString.startsWith("https://youtu.be")
            && searchString.contains("&")) {
            val suffix = searchString.subSequence(searchString.indexOf("&"), searchString.length - 1)
            searchString.removeSuffix(suffix)
        }

        val query = "ytsearch: $searchString"

        val track = lavaplayerManager.loadTrack(query)

        try{
            println("connecting")
            voiceConnectionsHandler.closeConnections(message.getGuild().id)
            voiceConnectionsHandler.connect(
                channelBehavior = channel,
                guildId = message.getGuild().id
            ) {
                audioProvider {
                    AudioFrame.fromData(player.provide().data)
                }
            }
            println("connected")
        } catch (ex: Exception) {
            //TODO: refactor, just for test
            println("error while voice connecting")
            throw Exception(ex.localizedMessage)
        }


        return track.info.uri
    }

    suspend fun stopSong(message: Message) {
        voiceConnectionsHandler.closeConnections(message.getGuild().id)

    }


    private suspend fun DefaultAudioPlayerManager.loadTrack(query: String): AudioTrack {
        val track = suspendCoroutine<AudioTrack> {
            this.loadItem(query, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    trackScheduler.queue(track)
                    it.resume(track)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
//                    for (track in playlist.tracks) {
//                        trackScheduler.queue(track!!)
//                    }
                    trackScheduler.queue(playlist.tracks[0])
                    it.resume(playlist.tracks[0])

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

