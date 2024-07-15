package laz.dimboba.dembobabot.voice

import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import laz.dimboba.dembobabot.channel.getGuildOrFail
import laz.dimboba.dembobabot.channel.getTextMessageChannelInstance
import org.koin.core.annotation.Singleton

private val logger = KotlinLogging.logger {}

@Singleton
class TrackSchedulerProvider {
    private val trackSchedulerMap: MutableMap<Snowflake, TrackScheduler> = mutableMapOf()

    init {
        logger.info { "TrackSchedulerProvider is started" }
    }

    //todo: add remove if track scheduler dies
    suspend fun getScheduler(event: MessageCreateEvent): TrackScheduler {
        val guild = event.getGuildOrFail()
        return trackSchedulerMap.getOrPut(guild.id) {
            TrackScheduler(
                getTextMessageChannelInstance(guild, "demboba-dj"),
                guild
            )
        }
    }
}