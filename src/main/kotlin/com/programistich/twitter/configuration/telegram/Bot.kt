package com.programistich.twitter.configuration.telegram

import com.programistich.twitter.routing.Router
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class Bot(
    private val telegramBotConfiguration: TelegramBotConfiguration,
    @Lazy private val router: Router
) : TelegramLongPollingBot() {

    override fun getBotToken(): String {
        return telegramBotConfiguration.token
    }

    override fun getBotUsername(): String {
        return telegramBotConfiguration.username
    }

    override fun onUpdateReceived(update: Update) {
        println(update)
        if (update.hasMessage()) {
            router.parseMessage(update)
        }
    }
}