package laz.dimboba.dembobabot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class TrackScheduler(
    private val player: AudioPlayer,
    private val connectionsHandler: VoiceConnectionsHandler

): AudioEventListener {

    private val audioTrackQueue = ArrayList<AudioTrack>()

    fun queue(track: AudioTrack){
        println("add track: ${track.info.title}" )
        audioTrackQueue.add(track)
    }

    fun play(){
        player.playTrack(audioTrackQueue.first())
        println(player.playingTrack.info.title)
        audioTrackQueue.removeFirst()
    }

    override fun onEvent(event: AudioEvent) {
        when(event) {
            is PlayerPauseEvent -> onPlayerPause(event.player)
            is PlayerResumeEvent -> onPlayerResume(event.player)
            is TrackStartEvent -> onTrackStart(event.player)
            is TrackEndEvent -> onTrackEnd(event.player)
            is TrackExceptionEvent -> onTrackException(event.player)
            is TrackStuckEvent -> onTrackStuck(event.player)
        }
    }
    private fun onPlayerPause(player: AudioPlayer){

    }
    private fun onPlayerResume(player: AudioPlayer){

    }
    private fun onTrackStart(player: AudioPlayer){

    }
    private fun onTrackEnd(player: AudioPlayer){

    }
    private fun onTrackException(player: AudioPlayer){

    }
    private fun onTrackStuck(player: AudioPlayer){

    }
}