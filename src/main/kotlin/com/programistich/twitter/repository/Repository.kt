package com.programistich.twitter.repository

import com.programistich.twitter.entity.TelegramChat
import com.programistich.twitter.entity.TwitterUser
import com.programistich.twitter.repository.jpa.TelegramChatRepository
import com.programistich.twitter.repository.jpa.TwitterUserRepository
import com.programistich.twitter.service.twitter.TwitterAccount
import com.programistich.twitter.service.twitter.TwitterClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Repository(
    private val twitterRepository: TwitterUserRepository,
    private val twitterClientService: TwitterClientService,
    private val telegramRepository: TelegramChatRepository,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun addTelegramChat(chatId: String) {
        val existChat = telegramRepository.findById(chatId).isPresent
        if (!existChat) {
            val telegramChat = TelegramChat(chatId)
            telegramRepository.save(telegramChat)
            logger.info("New telegram chat $chatId")
        }
    }

    fun addTwitterAccount(chatId: String, username: String, onExist: () -> Unit, onNotExist: () -> Unit) {
        val currentChat = telegramRepository.findById(chatId).orElseThrow()
        val twitterAccounts = currentChat.twitterUsers.filter { it.username == username }
        if (twitterAccounts.isNotEmpty()) onExist.invoke()
        else {
            val newTwitterAccount = TwitterUser(username)
            currentChat.twitterUsers.add(newTwitterAccount)
            newTwitterAccount.apply {
                chats.add(currentChat)
                lastLikeId = twitterClientService.lastLikeByUsername(username).id
                lastTweetId = twitterClientService.lastTweetByUsername(username).id
            }
            telegramRepository.save(currentChat)
            logger.info("New twitter account $username")
            onNotExist.invoke()
        }
    }

    fun getTwitterAccountByUsername(username: String): TwitterUser? {
        return twitterRepository.findById(username).orElse(null)
    }
}
