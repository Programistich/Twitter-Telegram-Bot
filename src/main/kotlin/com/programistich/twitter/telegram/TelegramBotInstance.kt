package com.programistich.twitter.telegram

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBotInstance(
    private val telegramBotConfiguration: TelegramBotConfiguration,
    @Lazy private val telegramBotRouting: TelegramBotRouting
) : TelegramLongPollingBot() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getBotToken(): String {
        return telegramBotConfiguration.token
    }

    override fun getBotUsername(): String {
        return telegramBotConfiguration.username
    }

    override fun onUpdateReceived(update: Update?) {
        logger.info("$update")
        if(update == null) throw NullPointerException("Telegram update null")
        telegramBotRouting.entryPointUpdate(update)
    }
}
