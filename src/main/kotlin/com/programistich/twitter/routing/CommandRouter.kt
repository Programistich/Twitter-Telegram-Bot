package com.programistich.twitter.routing

import com.programistich.twitter.common.Command
import com.programistich.twitter.common.annotation.Routing
import com.programistich.twitter.routing.def.DefaultCommandRouter
import com.programistich.twitter.service.telegram.TelegramBotExecutorService
import com.programistich.twitter.service.telegram.TelegramCommandService

@Routing
class CommandRouter(
    private val telegramCommandService: TelegramCommandService,
    private val telegramBotExecutorService: TelegramBotExecutorService
) : DefaultCommandRouter {

    override fun parseCommand(typeCommand: Command, chatId: String, textMessage: String) {
        when (typeCommand) {
            Command.START -> {
                telegramCommandService.registerChat(chatId)
            }
            Command.NEW -> {
                val command = typeCommand.command
                val username = textMessage.replace(command, "")
                if(username.isEmpty()) telegramBotExecutorService.sendTextMessage(chatId, "Поле не может быть пустым")
                else telegramCommandService.addTwitterUsernameToChat(chatId, username.trimStart())
            }
            Command.PING ->{
                telegramCommandService.pingChat(chatId)
            }
            Command.GET ->{
                val command = typeCommand.command
                val link = textMessage.replace(command, "")
                if(link.isEmpty()) telegramBotExecutorService.sendTextMessage(chatId, "Поле не может быть пустым")
                else telegramCommandService.getTweet(chatId, link.trimStart())
            }
            else -> {

            }
        }
    }

}