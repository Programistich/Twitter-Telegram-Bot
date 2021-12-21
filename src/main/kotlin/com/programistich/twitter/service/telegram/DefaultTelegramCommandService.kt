package com.programistich.twitter.service.telegram

interface DefaultTelegramCommandService {

    fun registerChat(chatId: String)

    fun addTwitterUsernameToChat(chatId: String, username: String, messageId: Int? = null)

    fun lastLikeTweetByUsername(chatId: String, username: String, messageId: Int? = null)

    fun pingChat(chatId: String)

    fun getTweet(chatId: String, link: String, messageId: Int)
}