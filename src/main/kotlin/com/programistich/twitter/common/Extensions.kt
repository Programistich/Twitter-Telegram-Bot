package com.programistich.twitter.common

import com.programistich.twitter.telegram.TelegramBotCommand
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update

object Extensions {

    fun Update.id(): String {
        if (this.hasChannelPost()) {
            return this.channelPost.chatId.toString()
        }
        return this.message.chatId.toString()
    }

    fun Update.getCommand(botName: String): TelegramBotCommand? {
        val entities: MutableList<MessageEntity> = if (this.hasMessage()) {
            this.message?.entities ?: return null
        } else if (this.hasChannelPost()) {
            this.channelPost?.entities ?: return null
        } else return null
        for (entity in entities) {
            if (entity.offset == 0 && entity.type == EntityType.BOTCOMMAND) {
                val parts = entity.text.split("@")
                if (parts.size == 1) {
                    return TelegramBotCommand.LOOKUP[parts[0]]
                }
                if (parts[1] == botName) {
                    return TelegramBotCommand.LOOKUP[parts[0]]
                }
            }
        }
        return null
    }
}
