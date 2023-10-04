package laz.dimboba.dembobabot.controller.impl.enums

import dev.kord.core.entity.Message
import laz.dimboba.dembobabot.exceptions.CannotFindMemberException
import laz.dimboba.dembobabot.voice.TrackScheduler

enum class MusicCommand {
    PLAY {
        override suspend fun exec(trackScheduler: TrackScheduler, args: List<String>, message: Message) {
            val member = message.getAuthorAsMemberOrNull()
            var searchString = ""
            for(i in 1..<args.size) {
                searchString += "${args[i]} "
            }
            trackScheduler.play(
                message.getGuild(),
                member?.getVoiceState()?.getChannelOrNull()
                    ?: throw CannotFindMemberException("There is no such member"),
                searchString
            )
        }

    },
    LEAVE {
        override suspend fun exec(trackScheduler: TrackScheduler, args: List<String>, message: Message) {
            trackScheduler.leave()
        }
    },
    PAUSE {
        override suspend fun exec(trackScheduler: TrackScheduler, args: List<String>, message: Message) {
            trackScheduler.pause()
        }
    },
    NEXT {
        override suspend fun exec(trackScheduler: TrackScheduler, args: List<String>, message: Message) {
            trackScheduler.nextSong()
        }
    },
    QUEUE {
        override suspend fun exec(trackScheduler: TrackScheduler, args: List<String>, message: Message) {
            //trackScheduler.showQueue()
        }
    },
    CLEAR {
        override suspend fun exec(trackScheduler: TrackScheduler, args: List<String>, message: Message) {
            trackScheduler.emptyQueue()
        }
    };


    abstract suspend fun exec(trackScheduler: TrackScheduler, args: List<String>, message: Message)
}