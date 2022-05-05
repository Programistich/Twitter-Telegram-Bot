package com.programistich.twitter.cache

import org.springframework.stereotype.Component

@Component
class TelegramCache {
    // tweetID - chatId - messageId
    private val cache: MutableMap<Long, MutableMap<String, Int>> = hashMapOf()

    fun get(tweetId: Long, chatId: String): Int? {
        val chats = cache[tweetId]
        if (chats == null) {
            cache[tweetId] = hashMapOf()
            return null
        }
        return cache[tweetId]?.get(chatId)
    }

    fun add(tweetId: Long, chatId: String, messageId: Int): Int {
        val chats = cache[tweetId]
        if (chats == null) {
            cache[tweetId] = hashMapOf()
        }
        cache[tweetId]?.set(chatId, messageId)
        return messageId
    }
}
