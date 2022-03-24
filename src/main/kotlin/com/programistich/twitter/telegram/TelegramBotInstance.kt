package com.programistich.twitter.telegram

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBotInstance(
    private val telegramBotConfiguration: TelegramBotConfiguration,
    @Lazy private val telegramBotRouting: TelegramBotRouting
) : TelegramLongPollingBot() {

    override fun getBotToken(): String {
        return telegramBotConfiguration.token
    }

    override fun getBotUsername(): String {
        return telegramBotConfiguration.username
    }

    override fun onUpdateReceived(update: Update?) {
        if(update == null) throw NullPointerException("Telegram update null")
        telegramBotRouting.entryPointUpdate(update)
    }
}
