package laz.dimboba.dembobabot.voice

import co.touchlab.stately.concurrency.AtomicBoolean
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Message
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.MessageChannel
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.rest.loadItem
import io.github.oshai.kotlinlogging.KotlinLogging
import laz.dimboba.dembobabot.controller.CommandAction
import laz.dimboba.dembobabot.exceptions.CannotFindMemberException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

//TODO: end refactoring (all messages to a new class)
//todo: maybe change skip and repeat mode work together
private val logger = KotlinLogging.logger { }

class TrackScheduler(
    private val messageChannel: MessageChannel,
    serverGuild: Guild
) : KoinComponent {

    private val lavakord: LavaKord by inject()
    private val audioTrackQueue = ArrayList<Track>()
    private var repeat: AtomicBoolean = AtomicBoolean(false)
    private val link = lavakord.getLink(serverGuild.id)
    private val player = link.player

    init {
        player.on<TrackEvent> {
            when(this) {
                is TrackStartEvent -> {
                    if(!repeat.value) {
                        messageChannel.createMessage("Now playing ${this.track.info.title}")
                    }
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
            "TrackScheduler for server ${serverGuild.name} with id ${serverGuild.id} has started"
        }

    }

    private suspend fun handleTrackEndEvent(event: TrackEndEvent) {
        when (event.reason) {
            Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.LOAD_FAILED -> {
                val message = "The ${event.track.info.title} track failed to load"
                messageChannel.createMessage(message)
                logger.warn { message }
            }
            Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.CLEANUP -> {
                messageChannel.createMessage(
                    "The ${event.track.info.title} track was ended because of player error"
                )
                logger.warn {
                    "The ${event.track.info.title} track was ended because the " +
                    "cleanup threshold for the audio player was reached"
                }
            }
            else -> {}
        }

        playNextTrack(event.track)
    }

    private suspend fun playNextTrack(track: Track) {
        if(repeat.value) {
            player.playTrack(audioTrackQueue.first())
            return
        }
        audioTrackQueue.removeFirst()
        if(audioTrackQueue.size == 0) {
            messageChannel.createMessage("The queue is empty. Bye-bye!")
            link.disconnectAudio()
            return
        }
        player.playTrack(audioTrackQueue.first())
    }

    @CommandAction("queue")
    suspend fun getQueue() {
        messageChannel.createMessage(
            audioTrackQueue
                .mapIndexed { index, track -> "${index + 1}) ${track.info.title}" }
                .joinToString("\n")
        )
    }

    @CommandAction("repeat", "repeat-mode")
    suspend fun repeat() {
        val answer: String
        if (repeat.compareAndSet(expected = true, new = false)) {
            answer = "Stop repeating mode"
        } else {
            repeat.value = true
            answer = "Repeat mode is on"
        }
        messageChannel.createMessage(answer)
    }

    @CommandAction("next", "skip")
    suspend fun nextSong() {
        val track = player.playingTrack
        player.stopTrack()
        messageChannel.createMessage("Track ${track?.info?.title ?: "unknown"} was skipped")
    }

    @CommandAction("empty")
    suspend fun emptyQueue() {
        audioTrackQueue.clear()
        messageChannel.createMessage("Queue was cleared")
    }

    @CommandAction("pause", "stop")
    suspend fun pause() {
        player.pause(true)
        messageChannel.createMessage("Player paused")
    }

    @CommandAction("leave")
    suspend fun leave() {
        link.disconnectAudio()
        audioTrackQueue.clear()
        player.stopTrack()
        messageChannel.createMessage("Bye-bye")
    }

    @CommandAction("play")
    suspend fun play(
        message: dev.kord.core.entity.Message,
        args: List<String>
    ) {
        var searchString = ""
        for (i in 1..<args.size) {
            searchString += "${args[i]} "
        }
        play(
            message.getGuild(),
            message.getAuthorAsMemberOrNull()?.getVoiceState()?.getChannelOrNull()
                ?: throw CannotFindMemberException("There is no such member"),
            searchString
        )
    }

    suspend private fun play(
        messageGuild: Guild,
        channel: BaseVoiceChannelBehavior,
        searchString: String
    ) {
        //todo: make it two different functions
        if (searchString.trim().isEmpty() && player.paused) {
            player.pause(false)
            messageChannel.createMessage(
                "Track ${player.playingTrack?.info?.title ?: "unknown"} is playing again"
            )
            return
        }

        loadYTTrack(searchString)
        if(player.playingTrack == null) {
            //todo: work with link is player in current channel or not
            val link = lavakord.getLink(messageGuild.id)
            link.connectAudio(channel.id.value)
            player.playTrack(audioTrackQueue.first())
        }
    }

    private fun createYTSearchString(searchString: String): String {
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
        when (val item = link.loadItem(createYTSearchString(searchString))) {
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


