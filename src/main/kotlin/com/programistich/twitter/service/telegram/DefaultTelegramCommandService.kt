package com.programistich.twitter.service.telegram

interface DefaultTelegramCommandService {

    fun registerChat(chatId: String)

    fun addTwitterUsernameToChat(chatId: String, username: String)

    fun lastLikeTweetByUsername(chatId: String, username: String)

    fun pingChat(chatId: String)
}