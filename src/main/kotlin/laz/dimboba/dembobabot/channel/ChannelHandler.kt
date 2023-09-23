package laz.dimboba.dembobabot.channel

import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.createNewsChannel
import dev.kord.core.behavior.channel.createTextChannel
import dev.kord.core.behavior.channel.createVoiceChannel
import dev.kord.core.behavior.createNewsChannel
import dev.kord.core.behavior.createTextChannel
import dev.kord.core.behavior.createVoiceChannel
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Category
import kotlinx.coroutines.flow.firstOrNull
import laz.dimboba.dembobabot.exceptions.GuildAlreadyExists

class ChannelHandler (
    val serverGuild: Guild
) {

    suspend fun createChannelIfNotExist (name: String, type: ChannelType, category: Category? = null) {
        val parentGuild = category?.guild ?: serverGuild


        val channelName = name.trim()
        if (parentGuild.channels.firstOrNull { channel -> channel.name == channelName } != null) {
            throw GuildAlreadyExists("There is channel with name: $channelName")
        }

        when (type) {
            ChannelType.TEXT -> parentGuild.createTextChannel(channelName)
            ChannelType.VOICE -> parentGuild.createVoiceChannel(channelName)
            ChannelType.NEWS -> parentGuild.createNewsChannel(channelName)
        }
    }

}