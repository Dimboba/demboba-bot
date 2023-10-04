package laz.dimboba.dembobabot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.voice.AudioFrame
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import java.lang.Integer.min
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//TODO: false message about empty queue after leave
//TODO: end refactoring (all messages to a new class)
private val logger = KotlinLogging.logger { }

class TrackScheduler(
    private val voiceConnectionsHandler: VoiceConnectionsHandler
) : AudioEventListener {

    private val lavaplayerManager = DefaultAudioPlayerManager()
    private val audioTrackQueue = ArrayList<AudioTrack>()
    private val player = lavaplayerManager.createPlayer()
    private var repeat: AudioTrack? = null

    private var voiceGuild: Guild? = null

    init {
        //lavaplayerManager.registerSourceManager(YoutubeAudioSourceManager())
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
        player.addListener(this)

        logger.info {
            "TrackScheduler is started"
        }
    }

    // interactions

    fun showQueue(message: Message) {
        if (audioTrackQueue.isEmpty()) {
            runBlocking {
                message.reply {
                    content = "Queue is empty"
                }
            }
            return
        }

        var answer: String = "There is ${audioTrackQueue.size} tracks in queue \n" +
                "First ${min(audioTrackQueue.size, 10)} tracks are: \n"
        for (i in 0..<min(audioTrackQueue.size, 10)) {
            answer += ("-) ${audioTrackQueue[i].info.title} \n")
        }


//        runBlocking {
//            message.reply {
//                content = answer
//            }
//        }
    }

    //TODO: fix because track's state = FINISHED
    fun repeat(message: Message) {
        val answer: String
        if (repeat != null) {
            answer = "Stop repeating"
            repeat = null
        } else {
            if (player.playingTrack == null) {
                answer = "Nothing to repeat"
            } else {
                repeat = player.playingTrack
                answer = "Repeating track: ${player.playingTrack.info.title}"
            }
        }

//        runBlocking {
//            messageChannel?.createMessage(
//                content = answer
//            )
//        }
    }

    fun nextSong() {
//
//        runBlocking {
//            message.reply {
//                content = "Track ${player.playingTrack.info.title} was skipped"
//            }
//        }

        player.stopTrack()

    }

    fun emptyQueue() {
        val size = audioTrackQueue.size
        audioTrackQueue.clear()

//        runBlocking {
//            message.reply {
//                content = "$size tracks was removed from queue"
//            }
//        }
    }

    fun pause() {
        player.isPaused = true
    }


    suspend fun leave() {
        voiceGuild?.id?.let { voiceConnectionsHandler.closeConnections(it) }
        audioTrackQueue.clear()
//        message.reply {
//            content = "Music player is shutdown"
//        }
    }

    @OptIn(KordVoice::class)
    suspend fun play(
        messageGuild: Guild,
        channel: BaseVoiceChannelBehavior,
        searchString: String
    ) {
        if (searchString.trim().isNotEmpty())
            loadYTSong(searchString)

        if (voiceConnectionsHandler.isConnected(messageGuild.id)) {
            if (searchString.trim().isEmpty() && player.isPaused)
                player.isPaused = false
            return
        }

        voiceGuild = messageGuild
        player.playTrack(audioTrackQueue.removeFirst())

        try {
            voiceConnectionsHandler.closeConnections(messageGuild.id)
            voiceConnectionsHandler.connect(
                channelBehavior = channel,
                guildId = messageGuild.id
            ) {
                audioProvider {
                    AudioFrame.fromData(player.provide()?.data)
                }
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to connect to VoiceChannel" }
            //TODO: refactor, just for test
            println("error while voice connecting")
            throw Exception(ex.localizedMessage)
        }
    }

    // event listeners

    override fun onEvent(event: AudioEvent) {
        when (event) {
            is PlayerPauseEvent -> onPlayerPause(event.player)
            is PlayerResumeEvent -> onPlayerResume(event.player)
            is TrackStartEvent -> onTrackStart(event.player)
            is TrackEndEvent -> onTrackEnd(event.player)
            is TrackExceptionEvent -> onTrackException(event.player)
            is TrackStuckEvent -> onTrackStuck(event.player)
        }
    }

    private fun onPlayerPause(player: AudioPlayer) {
//        runBlocking {
//            messageChannel?.createMessage(
//                "Player was stopped"
//            )
//        }
    }

    private fun onPlayerResume(player: AudioPlayer) {
//        runBlocking {
//            messageChannel?.createMessage(
//                "Continue playing: ${player.playingTrack.info.title}"
//            )
//        }
    }

    private fun onTrackStart(player: AudioPlayer) {
//        runBlocking {
//            messageChannel?.createMessage(
//                "Playing track: ${player.playingTrack.info.title}"
//            )
//        }
    }

    private fun onTrackEnd(player: AudioPlayer) {

        //TODO: fix (not working because track's state = FINISHED
//        if(repeat != null) {
//            repeat?.position = 0
//            player.playTrack(repeat)
//            return
//        }

        if (audioTrackQueue.isEmpty()) {
//            runBlocking {
//                voiceGuild?.id?.let { voiceConnectionsHandler.closeConnections(it) }
//                messageChannel?.createMessage(
//                    "Queue is empty"
//                )
//            }
            return
        }
        player.playTrack(audioTrackQueue.removeFirst())

    }

    private fun onTrackException(player: AudioPlayer) {

    }

    private fun onTrackStuck(player: AudioPlayer) {

    }

    // util functions

    private fun queueList(playlist: AudioPlaylist) {
        playlist.tracks.forEach { track -> audioTrackQueue.add(track) }
    }

    private fun queue(track: AudioTrack) {
        audioTrackQueue.add(track)
    }

    private suspend fun loadYTSong(searchString: String) {
        var isSearch = false
        val search = searchString.trimStart()
        val query: String = if (search.startsWith("https://www.youtube.com")
            || search.startsWith("https://youtu.be")
            || search.startsWith("https://youtube.com")
        ) {
            val suffix = if (search.contains("&")) {
                search.subSequence(search.indexOf("&"), search.length)
            } else {
                ""
            }
            search.removeSuffix(suffix)
        } else {
            isSearch = true
            "ytsearch: $search"
        }

        lavaplayerManager.loadTrack(
            query,
            isSearch
        )
    }


    private suspend fun DefaultAudioPlayerManager.loadTrack(
        query: String,
        isSearch: Boolean = false
    ) {
        val messageContent = suspendCoroutine<String> {
            this.loadItem(query, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    queue(track)
                    it.resume("Add track: ${track.info.title}")
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    if (isSearch) {
                        queue(playlist.tracks.first())
                        it.resume("Add track: ${playlist.tracks.first().info.title}")
                        return
                    }
                    queueList(playlist)
                    it.resume("Add ${playlist.tracks.size} tracks from ${playlist.name}")
                }


                override fun noMatches() {
                    it.resume("There is no such tracks")
                }

                override fun loadFailed(exception: FriendlyException?) {
                    logger.error(exception) { "Failed to load, query: $query" }
                    it.resume("An error occurred")
                }
            })
        }

//        message.reply {
//            content = messageContent
//        }
    }
}


