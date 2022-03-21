package com.programistich.twitterx_bot.service.telegram

import com.programistich.twitterx_bot.common.TypeCommand
import com.programistich.twitterx_bot.common.TypeMessageTelegram

interface TelegramExecutorService {

    fun sendTweet(
        chatId: String,
        typeMessage: TypeMessageTelegram?,
        typeCommand: TypeCommand,
        replyToMessageId: Int? = null,
    )

    fun sendTextMessage(chatId: String, text: String, replyToMessageId: Int? = null)
    fun sendPhotoMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?)
    fun sendAnimatedMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?)
    fun sendVideoMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?)
    fun sendManyMediaMessageByUrls(chatId: String, textMessage: String, urls: List<String>, replyToMessageId: Int?)
    fun deleteMessage(chatId: String, messageId: Int)
    fun sendStickerMessage(chatId: String, stickerId: String)
}
