package com.programistich.twitter.routing

import com.programistich.twitter.common.Extensions.getCommand
import com.programistich.twitter.common.Extensions.id
import com.programistich.twitter.common.Extensions.textMessage
import com.programistich.twitter.common.annotation.Routing
import com.programistich.twitter.configuration.telegram.TelegramBotConfiguration
import com.programistich.twitter.routing.def.DefaultRouter
import org.telegram.telegrambots.meta.api.objects.Update

@Routing
class Router(
    private val commandRouter: CommandRouter,
    private val telegramBotConfiguration: TelegramBotConfiguration
) : DefaultRouter {

    override fun parseMessage(update: Update) {
        val textMessage = update.textMessage() ?: ""
        val chatId = update.id()
        val command = update.getCommand(telegramBotConfiguration.username)
        if (command != null) commandRouter.parseCommand(command, chatId, textMessage)
    }

}