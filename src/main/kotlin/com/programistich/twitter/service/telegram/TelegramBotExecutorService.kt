package com.programistich.twitter.service.telegram

import com.programistich.twitter.common.TypeByTweet
import com.programistich.twitter.common.TypeMessage
import com.programistich.twitter.configuration.telegram.Bot
import com.programistich.twitter.service.translate.TranslateService
import com.programistich.twitter.service.twitter.TwitterClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.media.InputMedia
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto

@Service
class TelegramBotExecutorService(
    private val bot: Bot,
    private val twitterClientService: TwitterClientService,
    private val translateService: TranslateService
) : DefaultTelegramBotExecutorService {

    private val logger = LoggerFactory.getLogger(TelegramBotExecutorService::class.java)

    override fun sendTweet(
        chatId: String,
        parsedTweet: TypeMessage?,
        username: String,
        tweetId: Long,
        typeByTweet: TypeByTweet
    ) {
        val additionalText = createAdditionalText(username, typeByTweet, tweetId)
        when (parsedTweet) {
            is TypeMessage.TextMessage -> {
                val message = SendMessage()
                message.text = additionalText + translateService.translate(parsedTweet.text)
                message.chatId = chatId
                message.parseMode = "html"
                bot.execute(message)
                logger.info("Send text message $tweetId to $chatId")
            }
            is TypeMessage.PhotoMessage -> {
                val message = SendPhoto()
                message.chatId = chatId
                message.caption = additionalText + translateService.translate(parsedTweet.caption)
                message.photo = InputFile(parsedTweet.url)
                message.parseMode = "html"
                bot.execute(message)
                logger.info("Send photo message $tweetId to $chatId")
            }
            is TypeMessage.AnimatedMessage -> {
                val message = SendAnimation()
                message.chatId = chatId
                message.animation = InputFile(parsedTweet.url)
                message.parseMode = "html"
                message.caption = additionalText + translateService.translate(parsedTweet.caption)
                bot.execute(message)
                logger.info("Send video message $tweetId to $chatId")
            }
            is TypeMessage.VideoMessage -> {
                val message = SendVideo()
                message.chatId = chatId
                message.parseMode = "html"
                message.caption = additionalText + translateService.translate(parsedTweet.caption)
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
                media.caption = additionalText + translateService.translate(parsedTweet.caption)
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

    private fun createAdditionalText(username: String, typeByTweet: TypeByTweet, tweetId: Long): String {
        val nameUser = twitterClientService.nameUser(username)
        val linkUser = twitterClientService.urlUser(username)
        val tweet = twitterClientService.getTweetById(tweetId)
        val tweetAuthor = twitterClientService.getAuthorForTweet(tweet)
        val tweetLink = twitterClientService.getLinkOnTweet(tweetId, tweetAuthor)
        val userCollect = "<a href=\"$linkUser\">$nameUser</a>"
        var additionalText = ""
        if (typeByTweet == TypeByTweet.LIKE) {
            additionalText = "Новый <a href=\"$tweetLink\">лайк</a> от $userCollect\n\n"
        }
        if (typeByTweet == TypeByTweet.NEW) {
            additionalText = "Последний <a href=\"$tweetLink\">лайк</a> от $userCollect\n\n"
        }
        return additionalText
    }
}
