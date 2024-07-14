package laz.dimboba.dembobabot.channel

import laz.dimboba.dembobabot.controller.CommandAction
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@Singleton
class ChannelController : KoinComponent {

    private val channelHandler: ChannelHandler by inject()

    @CommandAction("create-channel", "createchannel")
    suspend fun createChannel(args: List<String>) =
        channelHandler.createChannelIfNotExist(
            name = args[2],
            type = MessageChannelType.valueOf(args[1].uppercase(Locale.getDefault())),
            categoryName = if (args.size > 3) args[3] else null
        )

    @CommandAction("delete-channel", "deletechannel")
    suspend fun deleteChannel(args: List<String>) =
        channelHandler.deleteChannelIfExist(
            name = args[2],
            type = MessageChannelType.valueOf(args[1].uppercase(Locale.getDefault())),
            categoryName = if (args.size > 3) args[3] else null
        )

    @CommandAction("create-category", "createcategory")
    suspend fun createCategory(args: List<String>) =
        channelHandler.createCategoryIfNotExist(args[1])

    @CommandAction("delete-category", "deletecategory")
    suspend fun deleteCategory(args: List<String>) =
        channelHandler.deleteCategoryIfExist(args[1])
}