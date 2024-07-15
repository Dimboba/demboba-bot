package laz.dimboba.dembobabot.channel

import dev.kord.core.event.message.MessageCreateEvent
import laz.dimboba.dembobabot.controller.CommandAction
import java.util.*

@CommandAction("create-channel", "createchannel")
suspend fun createChannel(args: List<String>, messageCreateEvent: MessageCreateEvent) =
    createChannelIfNotExist(
        name = args[2],
        type = MessageChannelType.valueOf(args[1].uppercase(Locale.getDefault())),
        categoryName = if (args.size > 3) args[3] else null,
        serverGuild = messageCreateEvent.getGuildOrFail()
    )

@CommandAction("delete-channel", "deletechannel")
suspend fun deleteChannel(args: List<String>, messageCreateEvent: MessageCreateEvent) =
    deleteChannelIfExist(
        name = args[2],
        type = MessageChannelType.valueOf(args[1].uppercase(Locale.getDefault())),
        categoryName = if (args.size > 3) args[3] else null,
        serverGuild = messageCreateEvent.getGuildOrFail()
    )

@CommandAction("create-category", "createcategory")
suspend fun createCategory(args: List<String>, messageCreateEvent: MessageCreateEvent) =
    createCategoryIfNotExist(
        name = args[1],
        serverGuild = messageCreateEvent.getGuildOrFail()
    )

@CommandAction("delete-category", "deletecategory")
suspend fun deleteCategory(args: List<String>, messageCreateEvent: MessageCreateEvent) =
    deleteCategoryIfExist(
        name = args[1],
        serverGuild = messageCreateEvent.getGuildOrFail()
    )
