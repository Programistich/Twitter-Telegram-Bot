package com.programistich.twitterx_bot.service.db

import com.programistich.twitterx_bot.entity.TelegramChat

interface DatabaseTelegramChatService {
    fun existChat(chatId: String): Boolean
    fun getChatById(chatId: String): TelegramChat?
    fun createChat(chat: TelegramChat)
    fun updateChat(chat: TelegramChat)
    fun existUsernameTwitter(chatId: String, username: String): Boolean
    fun getChatsByUsername(username: String): List<String>
    fun getAllChats(): List<TelegramChat>
}
