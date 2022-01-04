package com.programistich.twitter.routing

import com.programistich.twitter.common.Command
import com.programistich.twitter.common.Extensions.deleteCommand
import com.programistich.twitter.common.Extensions.getCommand
import com.programistich.twitter.common.Extensions.id
import com.programistich.twitter.configuration.telegram.TelegramBotConfiguration
import com.programistich.twitter.service.telegram.DefaultTelegramCommandService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class DefaultRouter(
    private val botConfiguration: TelegramBotConfiguration,
    private val telegramCommandService: DefaultTelegramCommandService
) : Router {

    override fun parseMessage(update: Update) {
        val command = update.getCommand(botConfiguration.username)
        if (command != null) {
            parseMessageWithCommand(command, update)
        } else if (update.message.text != null) parseTextMessage(update.message)
    }

    override fun parseMessageWithCommand(commandEnum: Command, update: Update) {
        val messageWithoutCommand = update.message.deleteCommand(botConfiguration.username, commandEnum)
        val message = update.message
        val chatId = update.id()
        when (commandEnum) {
            Command.START -> telegramCommandService.startCommand(chatId)
            Command.PING -> telegramCommandService.pingCommand(chatId)
            Command.DONATE -> telegramCommandService.donateCommand(message)
            Command.NEW_TWITTER_USER -> telegramCommandService.newTwitterUserCommand(message, messageWithoutCommand)
            Command.GET -> telegramCommandService.getTweetCommand(message, messageWithoutCommand)
            Command.HELP -> telegramCommandService.helpCommand(message)
            else -> {

            }
        }
    }

    override fun parseTextMessage(message: Message) {
        val twitterLink = existTwitterLink(message.text)
        if (twitterLink != null) {
            telegramCommandService.getTweetCommand(message, twitterLink)
        }
    }

    private fun existTwitterLink(message: String): String? {
        try {
            if (message.startsWith("https://twitter.com/")) return message
            val messageArray = message.split(" ")
            val link = messageArray[messageArray.lastIndex]
            return if (link.startsWith("https://twitter.com/")) link
            else null
        } catch (e: Exception) {
            return null
        }
    }

}