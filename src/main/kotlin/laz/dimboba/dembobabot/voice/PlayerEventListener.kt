package laz.dimboba.dembobabot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.core.entity.channel.MessageChannel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.math.log

private val logger = KotlinLogging.logger { }

@Singleton
class PlayerEventListener : TrackSchedulerListener, KoinComponent {

    private val messageChannel: MessageChannel by inject(named("MusicTextChannel"))

    init {
        logger.info {
            "PlayerEventListener is started with channel: ${messageChannel.data.name.value}"
        }
    }

    override fun onRepeat(isRepeating: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onTrackSkip(skippedTrack: AudioTrack) {
        runBlocking {
            messageChannel.createMessage(
                content = "Track ${skippedTrack.info.title} was skipped"
            )

        }
    }

    override fun onClearingQueue(previousQueue: List<AudioTrack>) {
        runBlocking {
            messageChannel.createMessage(
                content = "${previousQueue.size} tracks was removed from queue"
            )
        }
    }

    override fun onLeave() {
        logger.info { "PlayerEventListener is stopped" }
        runBlocking {
            messageChannel.createMessage(
                content = "Music player is shutdown"
            )
        }
    }

    override fun onAddTrack(track: AudioTrack) {
        logger.info { "Added track ${track.info.title}" }
        runBlocking {
            messageChannel.createMessage(
                content = "Add track: ${track.info.title}"
            )
        }
    }

    override fun onAddPlaylist(playlist: AudioPlaylist) {
        logger.info { playlist.name }
        runBlocking {
            messageChannel.createMessage(
                content = "Add ${playlist.tracks.size} tracks from ${playlist.name}"
            )
        }
    }

    override fun onNoMatches() {
    }

    override fun onLoadFailed() {
    }

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
        runBlocking {
            messageChannel.createMessage(
                "Player was stopped"
            )
        }
    }

    private fun onPlayerResume(player: AudioPlayer) {
        runBlocking {
            messageChannel.createMessage(
                "Continue playing: ${player.playingTrack.info.title}"
            )
        }
    }

    private fun onTrackStart(player: AudioPlayer) {
        runBlocking {
            messageChannel.createMessage(
                "Playing track: ${player.playingTrack.info.title}"
            )
        }
    }

    private fun onTrackEnd(player: AudioPlayer) {

    }

    private fun onTrackException(player: AudioPlayer) {

    }

    private fun onTrackStuck(player: AudioPlayer) {

    }
}