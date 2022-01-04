package com.programistich.twitter.service.telegram

import org.telegram.telegrambots.meta.api.objects.Message

interface TelegramCommandService {

    fun startCommand(chatId: String)
    fun newTwitterUserCommand(message: Message, username: String?)
    fun lastLikeTweetByUsernameCommand(message: Message, username: String)
    fun pingCommand(chatId: String)
    fun getTweetCommand(message: Message, link: String?)
    fun donateCommand(message: Message)
    fun helpCommand(message: Message)
}