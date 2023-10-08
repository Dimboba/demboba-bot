package laz.dimboba.dembobabot.channel

import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.*
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Category
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.TopGuildChannel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.firstOrNull
import laz.dimboba.dembobabot.exceptions.GuildAlreadyExists
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.*

private val logger = KotlinLogging.logger { }

//TODO: Exception if Category does not exist
@Singleton
class ChannelHandler : KoinComponent{

    private val serverGuild: Guild by inject(named("ServerGuild"))
    init {
        logger.info {
            "ChannelHandler is started"
        }
    }

    suspend fun createChannelIfNotExist (
        name: String,
        type: MessageChannelType,
        categoryName: String? = null): MessageChannel {
        val parentGuild = findCategoryByName(categoryName)
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

    suspend fun createCategoryIfNotExist(name: String) : Category {
        val channelName = name.trim()
        if (serverGuild.channels.firstOrNull { channel ->
                channel.name == channelName && channel.type == ChannelType.GuildCategory
            } != null) {
            throw GuildAlreadyExists("There is category with name: $channelName")
        }
        return serverGuild.createCategory(channelName)
    }

    suspend fun deleteChannelIfExist (
        name: String,
        type:MessageChannelType,
        categoryName: String? = null) {
        val parentGuild = findCategoryByName(categoryName)
        val channelName = name.trim().lowercase(Locale.getDefault())

        val channel = serverGuild.channels.firstOrNull { channel ->
            channel.name == channelName
                    && channel.type == type.kordType
                    && channel.data.parentId?.value == parentGuild?.id
        }
        channel?.delete()
    }

    suspend fun deleteCategoryIfExist (name:String) {
        val category = findCategoryByName(name) ?: return
        if (serverGuild.channels.firstOrNull {channel ->
            channel.data.parentId?.value == category.id
            } != null) {
            return
        }
        category.delete()
    }

    suspend fun getTextMessageChannelInstance(
        channelName: String,
        categoryName: String? = null): MessageChannel {
        return serverGuild.channels.firstOrNull { channel ->
                channel.name == channelName
                && channel.type == ChannelType.GuildText
                && channel.data.parentId?.value == findCategoryByName(categoryName)?.id
            } as MessageChannel? ?: return createChannelIfNotExist(
                channelName,
                MessageChannelType.TEXT,
                categoryName
            )
    }
    private suspend fun findCategoryByName(name: String?): TopGuildChannel? {
        //println(name)
        if (name == null) return null
        return serverGuild.channels.firstOrNull { channel ->
            channel.name.equals(name.trim(), ignoreCase = true)
                    && channel.type == ChannelType.GuildCategory
        }
    }
}

