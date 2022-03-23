package com.programistich.twitter.service.db

import com.programistich.twitter.entity.TelegramChat
import com.programistich.twitter.repository.TelegramChatRepository
import org.springframework.stereotype.Service

@Service
class DefaultDatabaseTelegramChatService(
    private val telegramChatRepository: TelegramChatRepository
) : DatabaseTelegramChatService {

    override fun existChat(chatId: String): Boolean {
        val chat = telegramChatRepository.findById(chatId).orElse(null)
        return chat != null
    }

    override fun getChatById(chatId: String): TelegramChat? {
        return telegramChatRepository.findById(chatId).orElse(null)
    }

    override fun createChat(chat: TelegramChat) {
        telegramChatRepository.save(chat)
    }

    override fun updateChat(chat: TelegramChat) {
        telegramChatRepository.save(chat)
    }

    override fun existUsernameTwitter(chatId: String, username: String): Boolean {
        val user = telegramChatRepository.findById(chatId).orElse(null) ?: return false
        return user.twitterUsers.map { it.username }.any { it == username }
    }

    override fun getChatsByUsername(username: String): List<String> {
        return telegramChatRepository.findAll().filter { telegramChat ->
            telegramChat.twitterUsers.map { it.username }.any { it == username }
        }.map { it.chatId }
    }

    override fun getAllChats(): List<TelegramChat> {
        return telegramChatRepository.findAll()
    }

}
