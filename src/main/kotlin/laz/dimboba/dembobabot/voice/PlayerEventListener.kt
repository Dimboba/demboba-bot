package laz.dimboba.dembobabot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.*

class PlayerEventListener () : AudioEventListener {
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

    }

    private fun onTrackException(player: AudioPlayer) {

    }

    private fun onTrackStuck(player: AudioPlayer) {

    }
}