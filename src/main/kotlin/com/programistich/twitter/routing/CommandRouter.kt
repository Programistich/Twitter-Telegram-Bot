package com.programistich.twitter.routing

import com.programistich.twitter.common.Command
import com.programistich.twitter.common.Extensions.id
import com.programistich.twitter.common.annotation.Routing
import com.programistich.twitter.routing.def.DefaultCommandRouter
import com.programistich.twitter.service.telegram.TelegramBotExecutorService
import com.programistich.twitter.service.telegram.TelegramCommandService
import org.telegram.telegrambots.meta.api.objects.Update

@Routing
class CommandRouter(
    private val telegramCommandService: TelegramCommandService,
    private val telegramBotExecutorService: TelegramBotExecutorService
) : DefaultCommandRouter {

    override fun parseCommand(typeCommand: Command, update: Update, textMessage: String) {
        val chatId = update.id()
        val textMessage = update.message.text
        val messageId = update.message.messageId
        when (typeCommand) {
            Command.START -> {
                telegramCommandService.registerChat(chatId)
            }
            Command.NEW -> {
                val array = textMessage.split(" ")
                if (array.size == 1) telegramBotExecutorService.sendTextMessage(chatId, "Поле не может быть пустым")
                else telegramCommandService.addTwitterUsernameToChat(
                    chatId,
                    array[array.lastIndex].trimStart(),
                    messageId
                )
                telegramBotExecutorService.deleteMessage(chatId, messageId)
            }
            Command.PING -> {
                telegramCommandService.pingChat(chatId)
            }
            Command.PICTURE -> {
                val array = textMessage.split(" ")
                if (array.size == 1) telegramCommandService.getRandomPicture(chatId)
                else telegramCommandService.getPictureByText(update.message, array[array.lastIndex].trimStart())
                telegramBotExecutorService.deleteMessage(chatId, messageId)
            }
            Command.DONATE -> {
                telegramCommandService.donate(chatId)
                telegramBotExecutorService.deleteMessage(chatId, messageId)
            }
            Command.GET -> {
                val array = textMessage.split(" ")
                if (array.size == 1) telegramBotExecutorService.sendTextMessage(chatId, "Поле не может быть пустым")
                else telegramCommandService.getTweet(update.message, array[array.lastIndex].trimStart())
                telegramBotExecutorService.deleteMessage(chatId, messageId)
            }
            else -> {

            }
        }
    }

}