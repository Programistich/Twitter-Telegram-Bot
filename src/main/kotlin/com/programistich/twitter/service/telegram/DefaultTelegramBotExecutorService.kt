package com.programistich.twitter.service.telegram

import com.programistich.twitter.common.TypeByTweet
import com.programistich.twitter.common.TypeMessage

interface DefaultTelegramBotExecutorService{

    fun sendTweet(chatId: String, parsedTweet: TypeMessage?, typeByTweet: TypeByTweet)

    fun sendTextMessage(chatId: String, text: String)
}