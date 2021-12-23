package com.programistich.twitter.service.db

import com.programistich.twitter.model.TelegramChat

interface DefaultDatabaseTelegramChatService {

    fun existChat(chatId: String): Boolean
    fun getChatById(chatId: String): TelegramChat?
    fun createChat(chat: TelegramChat)
    fun updateChat(chat: TelegramChat)
    fun existUsernameTwitter(chatId: String, username: String): Boolean
    fun getChatsByUsername(username: String): List<String>

}