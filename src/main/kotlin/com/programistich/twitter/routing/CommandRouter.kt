package com.programistich.twitter.routing

import com.programistich.twitter.common.Command
import com.programistich.twitter.common.annotation.Routing
import com.programistich.twitter.routing.def.DefaultCommandRouter
import com.programistich.twitter.service.telegram.TelegramCommandService

@Routing
class CommandRouter(
    private val telegramCommandService: TelegramCommandService
) : DefaultCommandRouter {

    override fun parseCommand(typeCommand: Command, chatId: String, textMessage: String) {
        when (typeCommand) {
            Command.START -> {
                telegramCommandService.registerChat(chatId)
            }
            Command.NEW -> {
                val command = typeCommand.command
                val username = textMessage.replace("$command ", "")
                telegramCommandService.addTwitterUsernameToChat(chatId, username)
            }
            else -> {

            }
        }
    }

}