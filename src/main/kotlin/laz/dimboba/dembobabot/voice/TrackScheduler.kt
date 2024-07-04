package laz.dimboba.dembobabot.voice

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.MessageChannel
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.rest.loadItem
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

//TODO: false message about empty queue after leave
//TODO: end refactoring (all messages to a new class)
private val logger = KotlinLogging.logger { }

@Singleton
class TrackScheduler : KoinComponent {


    private val voiceConnectionsHandler: VoiceConnectionsHandler by inject()
    private val lavakord: LavaKord by inject()
    private val messageChannel: MessageChannel by inject(named("MusicTextChannel"))
    private val serverGuild: Guild by inject(named("ServerGuild"))
    private val audioTrackQueue = ArrayList<Track>()
    private var repeat: Track? = null // todo make repeat mode
    var voiceGuild: BaseVoiceChannelBehavior? = null
        private set
    private val link = lavakord.getLink(serverGuild.id)
    private val player = link.player

    init {
        runBlocking {
            link.connectAudio(messageChannel.id.value)
        }

        player.on<TrackEvent> {
            when(this) {
                is TrackStartEvent -> {
                    messageChannel.createMessage("Now playing ${this.track.info.title}")
                }
                is TrackEndEvent -> {
                    handleTrackEndEvent(this)
                }
                is TrackStuckEvent -> {
                    logger.warn { "Track has stuck, reached ${this.threshold} of not getting audio frames" }
                    playNextTrack(this.track)
                }
                is TrackExceptionEvent -> {
                    logger.warn { "Track thrown an exception, cause: ${this.exception}" }
                }
            }
        }

        logger.info {
            "TrackScheduler is started"
        }

    }


    // interactions
    //TODO: fix because track's state = FINISHED

    private suspend fun handleTrackEndEvent(event: TrackEndEvent) {
//        if(!event.reason.mayStartNext) {
//            logger.error { "The $event stopped the music player, reason: ${event.reason}" }
//            messageChannel.createMessage("Player has died. Sorry...")
//            link.disconnectAudio()
//        }
        if(event.reason == dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.LOAD_FAILED) {
            val message = "The ${event.track.info.title} track failed to load"
            messageChannel.createMessage(message)
            logger.warn { message }
        }

        playNextTrack(event.track)
    }

    private suspend fun playNextTrack(track: Track) {
        val repeatingTrack = repeat
        if(repeatingTrack != null) {
            player.playTrack(repeatingTrack)
            return
        }
        if(audioTrackQueue.size == 0) {
            messageChannel.createMessage("The queue is empty. Bye-bye!")
            link.disconnectAudio()
            return
        }
        player.playTrack(audioTrackQueue.removeFirst())
    }

    fun getQueue(): ArrayList<Track> {
        return ArrayList(audioTrackQueue)
    }

    suspend fun repeat() {
        val answer: String
        if (repeat != null) {
            answer = "Stop repeating track ${repeat?.info?.title ?: "unknown"}"
            repeat = null
        } else {
            if (player.playingTrack == null) {
                answer = "Nothing to repeat"
            } else {
                repeat = player.playingTrack
                answer = "Repeating track: ${player.playingTrack?.info?.title ?: "unknown"}"
            }
        }
        messageChannel.createMessage(answer)
    }

    suspend fun nextSong() {
        val track = player.playingTrack
        player.stopTrack()
        messageChannel.createMessage("Track ${track?.info?.title ?: "unknown"} was skipped")
    }

    suspend fun emptyQueue() {
        val queue = ArrayList(audioTrackQueue)
        audioTrackQueue.clear()
        messageChannel.createMessage("Queue was cleared")
    }

    suspend fun pause() {
        player.pause(true)
        messageChannel.createMessage("Player paused")
    }

    suspend fun leave() {
//        voiceGuild?.id?.let { voiceConnectionsHandler.closeConnections(it) }
        link.disconnectAudio()
        audioTrackQueue.clear()
        voiceGuild = null
    }

    suspend fun play(
        messageGuild: Guild,
        channel: BaseVoiceChannelBehavior,
        searchString: String
    ) {
        if (searchString.trim().isEmpty() && player.paused) {
            player.pause(false)
            messageChannel.createMessage(
                "Track ${player.playingTrack?.info?.title ?: "unknown"} is playing again"
            )
            return
        }

        loadYTTrack(searchString)
        if(player.playingTrack == null) {
            voiceGuild = channel
            repeat = null
            val link = lavakord.getLink(messageGuild.id)
            link.connectAudio(channel.id.value)
            player.playTrack(audioTrackQueue.removeFirst())
        }
    }

    private  fun loadYTSong(searchString: String): String {
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
            "ytsearch:$search"
        }

        return query
    }

    private suspend fun loadYTTrack(
        searchString: String
    ) {
        when (val item = link.loadItem(loadYTSong(searchString))) {
            is LoadResult.TrackLoaded -> {
                messageChannel.createMessage("Added track ${item.data.info.title} to queue")
                audioTrackQueue.add(item.data)
            }
            is LoadResult.PlaylistLoaded -> {
                messageChannel.createMessage(
                    "Loaded playlist ${item.data.info.name} with ${item.data.tracks.size} tracks"
                )
                audioTrackQueue.addAll(item.data.tracks)
            }
            is LoadResult.SearchResult -> {
                messageChannel.createMessage(
                    "Found and added track ${item.data.tracks.first().info.title} to queue"
                )
                audioTrackQueue.add(item.data.tracks.first())
            }
            is LoadResult.NoMatches -> {
                messageChannel.createMessage("No match found for $searchString")
                logger.error { "No match found for $searchString" }
            }
            is LoadResult.LoadFailed -> {
                messageChannel.createMessage("Load failed for $searchString")
                logger.error { "Load failed for $searchString" }
            }
        }
    }
}


