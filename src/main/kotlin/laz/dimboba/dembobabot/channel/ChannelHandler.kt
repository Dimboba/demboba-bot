package laz.dimboba.dembobabot.channel

import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.*
import dev.kord.core.entity.channel.Category
import dev.kord.core.entity.channel.TopGuildChannel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.firstOrNull
import laz.dimboba.dembobabot.exceptions.GuildAlreadyExists

private val logger = KotlinLogging.logger { }

class ChannelHandler (
    private val serverGuild: GuildBehavior
) {

    init {
        logger.info {
            "ChannelHandler is started"
        }
    }

    suspend fun createChannelIfNotExist(name: String, type: MessageChannelType, categoryName: String? = null) {
        val parentGuild = findCategoryByName(categoryName)

        println(serverGuild.id)


        val channelName = name.trim()
        if (serverGuild.channels.firstOrNull { channel ->
                channel.name == channelName
                        && channel.type == type.kordType
                        && channel.data.parentId?.value == parentGuild?.id
            } != null) {
            throw GuildAlreadyExists("There is channel with name: $channelName")
        }

        when (type) {
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

    suspend fun createCategoryIfNotExist(name: String) {
        val channelName = name.trim()
        if (serverGuild.channels.firstOrNull { channel ->
                channel.name == channelName && channel.type == ChannelType.GuildCategory
            } != null) {
            throw GuildAlreadyExists("There is category with name: $channelName")
        }
        serverGuild.createCategory(channelName)
    }

    suspend fun deleteChannelIfExist(name: String, category: Category? = null) {
        val parentGuild = category?.guild ?: serverGuild
    }

    private suspend fun findCategoryByName(name: String?): TopGuildChannel? {
        println(name)
        if (name == null) return null
        return serverGuild.channels.firstOrNull { channel ->
            channel.name.equals(name.trim(), ignoreCase = true)
                    && channel.type == ChannelType.GuildCategory
        }
    }
}

