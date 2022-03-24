package com.programistich.twitter.common

import com.programistich.twitter.telegram.TelegramBotCommand
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

object Extensions {

    fun Update.id(): String {
        if (this.hasMessage()) {
            return this.message.chatId.toString()
        }
        if (this.hasChannelPost()) {
            return this.channelPost.chatId.toString()
        }
        return ""
    }

    fun Message.id(): String {
        return this.chatId.toString()
    }

    fun Update.textMessage(): String? {
        return this.message.text
    }

    fun Update.getCommand(botName: String): TelegramBotCommand? {
        val entities = if (this.hasMessage()) {
            this.message.entities
        } else if (this.hasChannelPost()) {
            this.channelPost.entities
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

    fun Message.deleteCommand(botName: String, telegramBotCommandName: TelegramBotCommand): String? {
        val entities = entities ?: return null
        val command = telegramBotCommandName.value
        for (entity in entities) {
            if (entity.offset == 0 && entity.type == EntityType.BOTCOMMAND) {
                val parts = entity.text.split("@")
                if (parts.size == 1) {
                    return this.text.replace(command, "").trim()
                }
                if (parts[1] == botName) {
                    return this.text.replace("$command@$botName", "").trim()
                }
            }
        }
        return null
    }
}
