package laz.dimboba.dembobabot.channel

import dev.kord.common.entity.ChannelType

enum class MessageChannelType(val kordType: ChannelType) {
    TEXT(ChannelType.GuildText),
    VOICE(ChannelType.GuildVoice),
    NEWS(ChannelType.GuildNews)
}