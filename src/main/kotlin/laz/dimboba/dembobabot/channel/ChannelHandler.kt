package laz.dimboba.dembobabot.channel

import dev.kord.core.behavior.channel.createNewsChannel
import dev.kord.core.behavior.channel.createTextChannel
import dev.kord.core.behavior.channel.createVoiceChannel
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Category
import kotlinx.coroutines.flow.firstOrNull
import laz.dimboba.dembobabot.exceptions.GuildAlreadyExists

class ChannelHandler (
    val serverGuild: Guild
) {

    suspend fun createChannelIfNotExist (category: Category, name: String, type: ChannelType) {
        val channelName = name.trim()
        if (category.channels.firstOrNull { channel -> channel.name == channelName } != null) {
            throw GuildAlreadyExists("There is channel with name: $channelName")
        }

        when (type) {
            ChannelType.TEXT -> category.createTextChannel(channelName)
            ChannelType.VOICE -> category.createVoiceChannel(channelName)
            ChannelType.NEWS -> category.createNewsChannel(channelName)
        }
    }

}