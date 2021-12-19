package com.programistich.twitter.service.telegram

import com.programistich.twitter.common.TypeByTweet
import com.programistich.twitter.common.TypeMessage
import com.programistich.twitter.configuration.telegram.Bot
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.media.InputMedia
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto

@Service
class TelegramBotExecutorService(
    private val bot: Bot
) : DefaultTelegramBotExecutorService {

    private val logger = LoggerFactory.getLogger(TelegramBotExecutorService::class.java)

    override fun sendTweet(chatId: String, parsedTweet: TypeMessage?, username: String, tweetId: Long, typeByTweet: TypeByTweet) {
        var additionalText = ""
        if(typeByTweet == TypeByTweet.LIKE) additionalText = "Новый лайк от $username\n\n"
        if(typeByTweet == TypeByTweet.NEW) additionalText = "Последний лайк от $username\n\n"
        when (parsedTweet) {
            is TypeMessage.TextMessage -> {
                val message = SendMessage()
                message.text = additionalText + parsedTweet.text
                message.chatId = chatId
                bot.execute(message)
                logger.info("Send text message $tweetId to $chatId")
            }
            is TypeMessage.PhotoMessage -> {
                val message = SendPhoto()
                message.chatId = chatId
                message.caption = additionalText + parsedTweet.caption
                message.photo = InputFile(parsedTweet.url)
                bot.execute(message)
                logger.info("Send photo message $tweetId to $chatId")
            }
            is TypeMessage.AnimatedMessage -> {
                val message = SendAnimation()
                message.chatId = chatId
                message.animation = InputFile(parsedTweet.url)
                message.caption = additionalText + parsedTweet.caption
                bot.execute(message)
                logger.info("Send video message $tweetId to $chatId")
            }
            is TypeMessage.VideoMessage -> {
                val message = SendVideo()
                message.chatId = chatId
                message.caption =additionalText +  parsedTweet.caption
                message.video = InputFile(parsedTweet.url)
                bot.execute(message)
                logger.info("Send video message $tweetId to $chatId")
            }
            is TypeMessage.ManyMediaMessage -> {
                val medias = parsedTweet.urlsMedia
                val message = SendMediaGroup()
                message.chatId = chatId
                val listMedia = arrayListOf<InputMedia>()
                var media = InputMediaPhoto()
                media.media = medias[0]
                media.caption = additionalText + parsedTweet.caption
                listMedia.add(media)
                for (i in 1 until medias.size) {
                    media = InputMediaPhoto()
                    media.media = medias[i]
                    listMedia.add(media)
                }
                message.medias = listMedia
                bot.execute(message)
                logger.info("Send many media message $tweetId to $chatId")
            }
            else -> {
                bot.execute(SendMessage(chatId, "Что-то пошло не так"))
                logger.info("Parse tweet null $tweetId to $chatId")
            }
        }
    }

    override fun sendTextMessage(chatId: String, text: String) {
        bot.execute(SendMessage(chatId, text))
    }
}
