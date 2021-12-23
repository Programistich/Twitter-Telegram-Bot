package com.programistich.twitter.routing

import com.programistich.twitter.common.Command
import com.programistich.twitter.common.Extensions.deleteCommand
import com.programistich.twitter.common.Extensions.getCommand
import com.programistich.twitter.common.Extensions.id
import com.programistich.twitter.configuration.telegram.TelegramBotConfiguration
import com.programistich.twitter.service.telegram.TelegramCommandService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class Router(
    private val botConfiguration: TelegramBotConfiguration,
    private val telegramCommandService: TelegramCommandService
) : DefaultRouter {

    override fun parseMessage(update: Update) {
        val command = update.getCommand(botConfiguration.username)
        if (command != null) {
            parseMessageWithCommand(command, update)
        } else parseTextMessage(update.message)
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
        if (twitterLink) {
            telegramCommandService.getTweetCommand(message, message.text)
        }
    }

    private fun existTwitterLink(message: String): Boolean {
        return try {
            message.startsWith("https://twitter.com/")
        } catch (e: Exception) {
            false
        }
    }

}