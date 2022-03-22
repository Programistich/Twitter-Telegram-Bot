package com.programistich.twitter.service.telegram

import com.programistich.twitter.common.TypeCommand
import com.programistich.twitter.common.TypeMessageTelegram

interface TelegramExecutorService {

    fun sendTweet(
        chatId: String,
        typeMessage: TypeMessageTelegram?,
        typeCommand: TypeCommand,
        replyToMessageId: Int? = null,
    ): Int

    fun sendTextMessage(chatId: String, text: String, replyToMessageId: Int? = null): Int
    fun sendPhotoMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?): Int
    fun sendAnimatedMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?): Int
    fun sendVideoMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?): Int
    fun sendManyMediaMessageByUrls(chatId: String, textMessage: String, urls: List<String>, replyToMessageId: Int?): Int
    fun deleteMessage(chatId: String, messageId: Int)
    fun sendStickerMessage(chatId: String, stickerId: String)
    fun sendTweetEntryPoint(tweetId: Long, chatId: String, author: String? = null)
}
