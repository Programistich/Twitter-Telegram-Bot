package com.programistich.twitter.service.telegram

import com.programistich.twitter.service.twitter.InternalTweet
import com.programistich.twitter.utils.TypeCommand
import com.programistich.twitter.telegram.TelegramMessageType

interface TelegramExecutorService {

    fun sendTweet(
        chatId: String,
        typeMessage: TelegramMessageType?,
        typeCommand: TypeCommand,
        replyToMessageId: Int? = null,
    ): Int

    fun sendTextMessage(chatId: String, text: String, replyToMessageId: Int? = null): Int
    fun sendPhotoMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?): Int
    fun sendAnimatedMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?): Int
    fun sendVideoMessageByUrl(
        chatId: String,
        textMessage: String,
        url: List<String>,
        replyToMessageId: Int?,
        tweetId: Long
    ): Int
    fun sendManyMediaMessageByUrls(chatId: String, textMessage: String, urls: List<String>, replyToMessageId: Int?): Int
    fun deleteMessage(chatId: String, messageId: Int)
    fun write(chatId: String)
    fun sendStickerMessage(chatId: String, stickerId: String)
    fun sendTweetEntryPoint(tweetId: Long, chatId: String, author: String? = null, isNew: Boolean = false, replyMessage: Int? = null)
    fun sendTweetEntryPoint(tweetInternal: InternalTweet, chatId: String, author: String? = null, isNew: Boolean = false, replyMessage: Int? = null)
}
