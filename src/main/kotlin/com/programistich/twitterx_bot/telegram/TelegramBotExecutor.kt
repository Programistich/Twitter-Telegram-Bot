package com.programistich.twitterx_bot.telegram

import com.programistich.twitterx_bot.twitter.Tweet
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.objects.InputFile

@Component
class TelegramBotExecutor(
    private val bot: TelegramBotInstance
) {

    fun sendTextMessage(chatId: String, replyId: Int? = null, message: String, preview: Boolean = false) {
        runCatching {
            val method = SendMessage()
            method.chatId = chatId
            replyId?.let {
                method.replyToMessageId = replyId
            }
            method.text = message
            method.parseMode = "html"
            if(!preview) method.disableWebPagePreview()
            bot.execute(method)
        }
    }

    fun sendStickerMessage(chatId: String, stickerId: String) {
        runCatching {
            val message = SendSticker()
            message.chatId = chatId
            message.sticker = InputFile(stickerId)
        }
    }

    fun sendTweet(tweet: Tweet, chatId: String) {

    }
}
