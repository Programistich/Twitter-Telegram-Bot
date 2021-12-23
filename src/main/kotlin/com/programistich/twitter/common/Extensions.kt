package com.programistich.twitter.common

import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

object Extensions {

    fun Update.id(): String {
        return this.message.chatId.toString()
    }

    fun Message.id(): String {
        return this.chatId.toString()
    }

    fun Update.textMessage(): String? {
        return this.message.text
    }

    fun Update.getCommand(botName: String): Command? {
        val entities = this.message.entities ?: return null
        for (entity in entities) {
            if (entity.offset == 0 && entity.type == EntityType.BOTCOMMAND) {
                val parts = entity.text.split("@")
                if (parts.size == 1) {
                    return Command.LOOKUP[parts[0]]
                }
                if (parts[1] == botName) {
                    return Command.LOOKUP[parts[0]]
                }
            }
        }
        return null
    }

    fun Message.deleteCommand(botName: String, commandName: Command): String? {
        val entities = entities ?: return null
        val command = commandName.command
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