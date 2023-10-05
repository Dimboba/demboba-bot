package laz.dimboba.dembobabot.voice

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

interface TrackSchedulerListener : AudioEventListener {
    fun onRepeat(isRepeating: Boolean)
    fun onTrackSkip(skippedTrack: AudioTrack)
    fun onClearingQueue(previousQueue: List<AudioTrack>)
    fun onLeave()
    fun onAddTrack(track: AudioTrack)
    fun onAddPlaylist(playlist: AudioPlaylist)
    fun onNoMatches()
    fun onLoadFailed()
}