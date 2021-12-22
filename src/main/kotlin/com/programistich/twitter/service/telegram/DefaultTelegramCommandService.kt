package com.programistich.twitter.service.telegram

import org.telegram.telegrambots.meta.api.objects.Message

interface DefaultTelegramCommandService {

    fun registerChat(chatId: String)

    fun addTwitterUsernameToChat(chatId: String, username: String, messageId: Int? = null)

    fun lastLikeTweetByUsername(chatId: String, username: String, messageId: Int? = null)

    fun pingChat(chatId: String)

    fun getTweet(message: Message, link: String)

    fun getRandomPicture(chatId: String)

    fun getPictureByText(message: Message?, query: String)

    fun donate(chatId: String)
}