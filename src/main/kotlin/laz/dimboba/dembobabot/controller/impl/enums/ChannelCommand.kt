package laz.dimboba.dembobabot.controller.impl.enums

import laz.dimboba.dembobabot.channel.ChannelHandler
import laz.dimboba.dembobabot.channel.MessageChannelType
import java.util.*

enum class ChannelCommand {
    CREATE_CHANNEL {
        override suspend fun exec(channelHandler: ChannelHandler, args: List<String>) {
            channelHandler.createChannelIfNotExist(
                name = args[2],
                type = MessageChannelType.valueOf(args[1].uppercase(Locale.getDefault())),
                categoryName = if (args.size > 3) args[3] else null
            )
        }
    },

    DELETE_CHANNEL {
        override suspend fun exec(channelHandler: ChannelHandler, args: List<String>) {
            channelHandler.deleteChannelIfExist(
                name = args[2],
                type = MessageChannelType.valueOf(args[1].uppercase(Locale.getDefault())),
                categoryName = if (args.size > 3) args[3] else null
            )
        }
    },

    CREATE_CATEGORY {
        override suspend fun exec(channelHandler: ChannelHandler, args: List<String>) {
            channelHandler.createCategoryIfNotExist(args[1])
        }
    },

    DELETE_CATEGORY {
        override suspend fun exec(channelHandler: ChannelHandler, args: List<String>) {
            channelHandler.deleteCategoryIfExist(args[1])
        }
    };

    abstract suspend fun exec(channelHandler: ChannelHandler, args: List<String>)
}