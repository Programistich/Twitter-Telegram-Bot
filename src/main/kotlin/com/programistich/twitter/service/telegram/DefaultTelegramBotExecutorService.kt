package com.programistich.twitter.service.telegram

import com.programistich.twitter.common.TypeByTweet
import com.programistich.twitter.common.TypeMessage

interface DefaultTelegramBotExecutorService {

    fun sendTweet(
        chatId: String,
        parsedTweet: TypeMessage?,
        typeByTweet: TypeByTweet,
        replyToMessageId: Int? = null,
    )

    fun sendTextMessage(chatId: String, text: String, replyToMessageId: Int? = null)

    fun deleteMessage(chatId: String, messageId: Int)
}