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
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.voice.AudioFrame
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

//TODO: false message about empty queue after leave
//TODO: end refactoring (all messages to a new class)
private val logger = KotlinLogging.logger { }

class TrackScheduler (
    private val voiceConnectionsHandler: VoiceConnectionsHandler
) : AudioEventListener {

    private val lavaplayerManager = DefaultAudioPlayerManager()
    private val audioTrackQueue = ArrayList<AudioTrack>()
    private val player = lavaplayerManager.createPlayer()
    private val listeners = ArrayList<TrackSchedulerListener>()
    private var repeat: AudioTrack? = null
    var voiceGuild: BaseVoiceChannelBehavior? = null
        private set

    init {
        //lavaplayerManager.registerSourceManager(YoutubeAudioSourceManager())
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
        player.addListener(this)

        logger.info {
            "TrackScheduler is started"
        }
    }

    fun addListener(listener: TrackSchedulerListener) {
        listeners.add(listener)
        player.addListener(listener)

        logger.info {
            "Add listener: ${listener::class.simpleName}"
        }
    }

    // interactions
    //TODO: fix because track's state = FINISHED

    fun getQueue(): ArrayList<AudioTrack> {
        return ArrayList(audioTrackQueue)
    }
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
}

    fun nextSong() {
        val track = player.playingTrack
        player.stopTrack()
        listeners.forEach {
            listener -> listener.onTrackSkip(track)
        }
    }

    fun emptyQueue() {
        val queue = ArrayList(audioTrackQueue)
        audioTrackQueue.clear()
        listeners.forEach {
            listener -> listener.onClearingQueue(queue)
        }
    }

    fun pause() {
        player.isPaused = true
    }


    suspend fun leave() {
        voiceGuild?.id?.let { voiceConnectionsHandler.closeConnections(it) }
        audioTrackQueue.clear()
        listeners.forEach {
            listener -> listener.onLeave()
        }
        voiceGuild = null
    }

    @OptIn(KordVoice::class)
    suspend fun play(
        messageGuild: Guild,
        channel: BaseVoiceChannelBehavior,
        searchString: String
    ) {
        if (searchString.trim().isNotEmpty())
            loadYTSong(searchString)

        if (voiceConnectionsHandler.isConnected(channel.id)) {
            if (searchString.trim().isEmpty() && player.isPaused)
                player.isPaused = false
            return
        }

        voiceGuild = channel
        player.playTrack(audioTrackQueue.removeFirst())

        try {
            voiceConnectionsHandler.closeConnections(channel.id)
            voiceConnectionsHandler.connect(
                channelBehavior = channel,
                id = channel.id
            ) {
                audioProvider {
                    AudioFrame.fromData(player.provide()?.data)
                }
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to connect to VoiceChannel" }
            //TODO: refactor, just for test
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
    }

    private fun onPlayerResume(player: AudioPlayer) {
    }

    private fun onTrackStart(player: AudioPlayer) {
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
        //TODO: maybe fix coroutine call (ask Ars)
        suspendCoroutine {
            this.loadItem(query, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    queue(track)
                    listeners.forEach {
                        listener -> listener.onAddTrack(track)
                    }
                    it.resume(true)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    if (isSearch) {
                        queue(playlist.tracks.first())
                        listeners.forEach {
                            listener -> listener.onAddTrack(playlist.tracks.first())
                        }
                        it.resume(true)
                        return
                    }
                    queueList(playlist)
                    listeners.forEach {
                        listener -> listener.onAddPlaylist(playlist)
                    }
                    it.resume(true)
                }

                override fun noMatches() {
                    listeners.forEach {
                        listener -> listener.onNoMatches()
                    }
                    it.resume(true)
                }

                override fun loadFailed(exception: FriendlyException?) {
                    logger.error(exception) { "Failed to load, query: $query" }
                    listeners.forEach {
                        listener -> listener.onLoadFailed()
                    }
                    it.resume(true)
                }
            })
        }
    }
}


