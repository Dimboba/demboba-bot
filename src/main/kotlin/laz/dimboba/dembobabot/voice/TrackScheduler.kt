package laz.dimboba.dembobabot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Message
import dev.kord.voice.AudioFrame

class TrackScheduler(
    private val player: AudioPlayer,
    private val voiceConnectionsHandler: VoiceConnectionsHandler

): AudioEventListener {

    private val audioTrackQueue = ArrayList<AudioTrack>()

    fun queue(track: AudioTrack){
        //println("add track: ${track.info.title}" )
        audioTrackQueue.add(track)

    }

    fun queueList(playlist: AudioPlaylist) {
        playlist.tracks.forEach { track -> audioTrackQueue.add(track) }
    }

    @OptIn(KordVoice::class)
    suspend fun play(message: Message, channel: BaseVoiceChannelBehavior){
        if(voiceConnectionsHandler.isConnected(message.getGuild().id))
            return
        player.playTrack(audioTrackQueue.first())
        println(player.playingTrack.info.title)
        audioTrackQueue.removeFirst()
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