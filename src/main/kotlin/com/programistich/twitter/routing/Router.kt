package com.programistich.twitter.routing

import com.programistich.twitter.telegram.TelegramBotCommand
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

interface Router {
    fun parseMessage(update: Update)
    fun parseMessageWithCommand(telegramBotCommandEnum: TelegramBotCommand, update: Update)
    fun parseTextMessage(message: Message)
}
