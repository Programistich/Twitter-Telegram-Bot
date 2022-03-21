package com.programistich.twitterx_bot.telegram

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBotInstance(
    private val configuration: TelegramBotConfiguration,
    @Lazy private val router: TelegramRouter,
) : TelegramLongPollingBot() {

    override fun getBotToken(): String {
        return configuration.token
    }

    override fun getBotUsername(): String {
        return configuration.username
    }

    override fun onUpdateReceived(update: Update?) {
        if (update != null) router.obtainUpdate(update)
    }
}
