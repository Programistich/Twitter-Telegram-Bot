package com.programistich.twitter.telegram

import com.programistich.twitter.common.Extensions.getCommand
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBotRouting(
    private val botConfiguration: TelegramBotConfiguration,
    private val telegramBotService: TelegramBotService,
) {

    fun entryPointUpdate(update: Update) {
        val message: String? = if (update.hasMessage()) {
            update.message.text
        } else if (update.hasChannelPost()) {
            update.channelPost.text
        } else return
        val command = update.getCommand(botConfiguration.username)
        if(message == null) return
        if (command != null) {
            val textCommand = message.split(" ")[0]
            val messageWithoutCommand = message.replace(textCommand, "").trim()
            when (command) {
                TelegramBotCommand.START -> telegramBotService.startCommand(update)
                TelegramBotCommand.HELP -> telegramBotService.helpCommand(update)
                TelegramBotCommand.PING -> telegramBotService.pingCommand(update)
                TelegramBotCommand.DONATE -> telegramBotService.donateCommand(update)
                TelegramBotCommand.ADD -> {
                    telegramBotService.addTwitterAccountCommand(
                        update = update,
                        username = messageWithoutCommand
                    )
                }
                TelegramBotCommand.GET -> {
                    isTweet(messageWithoutCommand) { tweetId ->
                        telegramBotService.getTweetCommand(update = update, tweetId = tweetId)
                    }
                }
                TelegramBotCommand.STOCKS -> telegramBotService.stocksCommand(update)
            }
        } else {
            isTweet(message) { tweetId ->
                telegramBotService.getTweetCommand(update = update, tweetId = tweetId)
            }
        }
    }

    private fun isTwitterAccount(message: String, onAction: (String) -> Unit) {
        val nameAccount = message
            .replace(".*twitter.com/".toRegex(), "")
            .replace("\\W.*".toRegex(), "")
        onAction.invoke(nameAccount)
    }

    private fun isTweet(message: String, onAction: (Long) -> Unit) {
        runCatching {
            if (message.contains("twitter.com") && message.contains("status")) {
                val idTweet = message
                    .replace(".*status/".toRegex(), "")
                    .replace("\\W.*".toRegex(), "")
                    .replace("\\D.*".toRegex(), "")
                onAction.invoke(idTweet.toLong())
            }
        }
    }
}
