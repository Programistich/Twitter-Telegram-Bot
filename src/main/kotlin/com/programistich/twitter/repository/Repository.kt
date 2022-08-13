package com.programistich.twitter.repository

import com.programistich.twitter.entity.TelegramChat
import com.programistich.twitter.entity.TwitterUser
import com.programistich.twitter.repository.jpa.TelegramChatRepository
import com.programistich.twitter.repository.jpa.TwitterUserRepository
import com.programistich.twitter.service.twitter.TwitterService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Repository(
    private val twitterRepository: TwitterUserRepository,
    private val twitterService: TwitterService,
    private val telegramRepository: TelegramChatRepository,
) {
    private val current: MutableMap<Long, MutableMap<Long, Int>> = mutableMapOf()
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun addTelegramChat(chatId: String, isChannel: Boolean) {
        val existChat = telegramRepository.findById(chatId).isPresent
        if (!existChat) {
            val telegramChat = TelegramChat(chatId, isChannel)
            telegramRepository.save(telegramChat)
            logger.info("New telegram chat $chatId")
        }
    }

    fun addTwitterAccount(chatId: String, username: String): Boolean {
        val currentChat = telegramRepository.findById(chatId).orElseThrow()
        val twitterAccounts = currentChat.twitterUsers.filter { it.username == username }
        return if (twitterAccounts.isNotEmpty()) true
        else {
            val newTwitterAccount = twitterRepository.findById(username).orElse(TwitterUser(username))
            newTwitterAccount.apply {
                chats.add(currentChat)
                lastLikeId = twitterService.lastLikeByUsername(username).first().id
                lastTweetId = twitterService.lastTweetByUsername(username).first().id
            }
            twitterRepository.save(newTwitterAccount)
            logger.info("New twitter account $username")
            false
        }
    }

    fun getTwitterAccountByUsername(username: String): TwitterUser? {
        return twitterRepository.findById(username).orElse(null)
    }
}
