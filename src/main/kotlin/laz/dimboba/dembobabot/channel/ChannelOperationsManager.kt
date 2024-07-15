package laz.dimboba.dembobabot.channel

import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.createCategory
import dev.kord.core.behavior.createNewsChannel
import dev.kord.core.behavior.createTextChannel
import dev.kord.core.behavior.createVoiceChannel
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Category
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.TopGuildChannel
import dev.kord.core.event.message.MessageCreateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.firstOrNull
import laz.dimboba.dembobabot.exceptions.GuildAlreadyExists
import laz.dimboba.dembobabot.exceptions.GuildDoesNotExistException
import java.util.*

private val logger = KotlinLogging.logger { }

//TODO: Exception if Category does not exist

suspend fun createChannelIfNotExist(
    serverGuild: Guild,
    name: String,
    type: MessageChannelType,
    categoryName: String? = null
): MessageChannel {
    val parentGuild = findCategoryByName(serverGuild, categoryName)
    val channelName = name.trim().lowercase(Locale.getDefault())
    if (serverGuild.channels.firstOrNull { channel ->
            channel.name == channelName
                    && channel.type == type.kordType
                    && channel.data.parentId?.value == parentGuild?.id
        } != null) {
        throw GuildAlreadyExists("There is channel with name: $channelName")
    }
    return when (type) {
        MessageChannelType.TEXT -> serverGuild.createTextChannel(
            channelName
        ) {
            parentId = parentGuild?.id
        }

        MessageChannelType.VOICE -> serverGuild.createVoiceChannel(
            channelName
        ) {
            parentId = parentGuild?.id
        }

        MessageChannelType.NEWS -> serverGuild.createNewsChannel(
            channelName
        ) {
            parentId = parentGuild?.id
        }
    }
}

suspend fun createCategoryIfNotExist(serverGuild: Guild, name: String): Category {
    val channelName = name.trim()
    if (serverGuild.channels.firstOrNull { channel ->
            channel.name == channelName && channel.type == ChannelType.GuildCategory
        } != null) {
        throw GuildAlreadyExists("There is category with name: $channelName")
    }
    return serverGuild.createCategory(channelName)
}

suspend fun deleteChannelIfExist(
    serverGuild: Guild,
    name: String,
    type: MessageChannelType,
    categoryName: String? = null
) {
    val parentGuild = findCategoryByName(serverGuild, categoryName)
    val channelName = name.trim().lowercase(Locale.getDefault())

    val channel = serverGuild.channels.firstOrNull { channel ->
        channel.name == channelName
                && channel.type == type.kordType
                && channel.data.parentId?.value == parentGuild?.id
    }
    channel?.delete()
}

suspend fun deleteCategoryIfExist(serverGuild: Guild, name: String) {
    val category = findCategoryByName(serverGuild, name) ?: return
    if (serverGuild.channels.firstOrNull { channel ->
            channel.data.parentId?.value == category.id
        } != null) {
        return
    }
    category.delete()
}

suspend fun getTextMessageChannelInstance(
    serverGuild: Guild,
    channelName: String,
    categoryName: String? = null
): MessageChannel {
    return serverGuild.channels.firstOrNull { channel ->
        channel.name == channelName
                && channel.type == ChannelType.GuildText
                && channel.data.parentId?.value == findCategoryByName(serverGuild, categoryName)?.id
    } as MessageChannel? ?: return createChannelIfNotExist(
        serverGuild,
        channelName,
        MessageChannelType.TEXT,
        categoryName
    )
}

private suspend fun findCategoryByName(serverGuild: Guild, name: String?): TopGuildChannel? {
    if (name == null) return null
    return serverGuild.channels.firstOrNull { channel ->
        channel.name.equals(name.trim(), ignoreCase = true)
                && channel.type == ChannelType.GuildCategory
    }
}

suspend fun MessageCreateEvent.getGuildOrFail(): Guild =
    getGuildOrNull() ?: throw GuildDoesNotExistException("Could not determine server guild")