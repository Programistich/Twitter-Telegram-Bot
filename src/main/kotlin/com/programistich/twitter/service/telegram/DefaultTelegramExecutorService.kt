package com.programistich.twitter.service.telegram

import com.programistich.twitter.common.TypeCommand
import com.programistich.twitter.common.TypeMessageTelegram
import com.programistich.twitter.configuration.telegram.Bot
import com.programistich.twitter.service.translate.TranslateService
import com.programistich.twitter.service.twitter.TwitterClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.media.InputMedia
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Service
class DefaultTelegramExecutorService(
    private val bot: Bot,
    private val twitterClientService: TwitterClientService,
    private val translateService: TranslateService
) : TelegramExecutorService {

    private val logger = LoggerFactory.getLogger(DefaultTelegramExecutorService::class.java)

    override fun sendTweet(
        chatId: String,
        typeMessage: TypeMessageTelegram?,
        typeCommand: TypeCommand,
        replyToMessageId: Int?,
    ) {
        val headerText = headerText(typeCommand)
        when (typeMessage) {
            is TypeMessageTelegram.TextMessage -> {
                val textMessage = formatText(headerText, typeMessage.text)
                sendTextMessage(chatId, textMessage, replyToMessageId)
            }
            is TypeMessageTelegram.PhotoMessage -> {
                val textMessage = formatText(headerText, typeMessage.text)
                sendPhotoMessageByUrl(chatId, textMessage, typeMessage.url, replyToMessageId)
            }
            is TypeMessageTelegram.AnimatedMessage -> {
                val textMessage = formatText(headerText, typeMessage.text)
                sendAnimatedMessageByUrl(chatId, textMessage, typeMessage.url, replyToMessageId)
            }
            is TypeMessageTelegram.VideoMessage -> {
                val textMessage = formatText(headerText, typeMessage.text)
                sendVideoMessageByUrl(chatId, textMessage, typeMessage.url, replyToMessageId)
            }
            is TypeMessageTelegram.ManyMediaMessage -> {
                val textMessage = formatText(headerTextForManyMedia(typeCommand), typeMessage.text)
                sendManyMediaMessageByUrls(chatId, textMessage, typeMessage.urlsMedia, replyToMessageId)
            }
            else -> {
                bot.execute(SendMessage(chatId, "Что-то пошло не так"))
            }
        }
    }

    private fun formatText(additionalText: String, textTweet: String): String {
        val translateText = translateService.translateText(textTweet.trim())
        val formatUsername = twitterClientService.usernameToLink(translateText)
        return additionalText + "\n\n" + formatUsername
    }

    override fun sendTextMessage(chatId: String, text: String, replyToMessageId: Int?) {
        val message = SendMessage()
        message.text = text
        message.chatId = chatId
        message.parseMode = "html"
        message.disableWebPagePreview = true
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        message.disableWebPagePreview = true
        bot.execute(message)
        logger.info("Send text message to $chatId")
    }

    override fun sendPhotoMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?) {
        val message = SendPhoto()
        message.chatId = chatId
        message.caption = textMessage
        message.photo = InputFile(url)
        message.parseMode = "html"
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        bot.execute(message)
        logger.info("Send photo message to $chatId")
    }

    override fun sendAnimatedMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?) {
        val message = SendAnimation()
        message.chatId = chatId
        message.caption = textMessage
        message.animation = InputFile(url)
        message.parseMode = "html"
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        bot.execute(message)
        logger.info("Send animated message to $chatId")
    }

    override fun sendVideoMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?) {
        val message = SendVideo()
        message.chatId = chatId
        message.caption = textMessage
        message.video = InputFile(url)
        message.parseMode = "html"
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        bot.execute(message)
        logger.info("Send video message to $chatId")
    }

    override fun sendManyMediaMessageByUrls(
        chatId: String,
        textMessage: String,
        urls: List<String>,
        replyToMessageId: Int?
    ) {
        val message = SendMediaGroup()
        message.chatId = chatId
        val listMedia = arrayListOf<InputMedia>()
        var media = InputMediaPhoto()
        media.media = urls[0]
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        media.caption = textMessage
        listMedia.add(media)
        for (i in 1 until urls.size) {
            media = InputMediaPhoto()
            media.media = urls[i]
            listMedia.add(media)
        }
        message.medias = listMedia
        bot.execute(message)
        logger.info("Send many media message to $chatId")
    }

    override fun sendStickerMessage(chatId: String, stickerId: String) {
        val message = SendSticker()
        message.chatId = chatId
        message.sticker = InputFile(stickerId)
        bot.execute(message)
        logger.info("Send sticker message to $chatId")
    }

    override fun deleteMessage(chatId: String, messageId: Int) {
        try {
            val delete = DeleteMessage()
            delete.chatId = chatId
            delete.messageId = messageId
            bot.execute(delete)
            logger.info("Delete message in $chatId")
        } catch (e: TelegramApiException) {
            logger.info("Cant delete msg")
        }

    }

    private fun headerText(typeCommand: TypeCommand): String {
        return when (typeCommand) {
            is TypeCommand.Get -> {
                val nameUser = twitterClientService.nameUser(typeCommand.username)
                val linkUser = twitterClientService.urlUser(typeCommand.username)
                val htmlUsername = "<a href=\"$linkUser\">$nameUser</a>"

                val author = typeCommand.author
                val link = typeCommand.link
                val htmlLink = "<a href=\"$link\">ссылке</a>"

                "Твит по $htmlLink от $htmlUsername by $author"
            }
            is TypeCommand.Like -> {
                val nameAction = twitterClientService.nameUser(typeCommand.username)
                val linkOnUserAction = twitterClientService.urlUser(typeCommand.username)
                val htmlUserNameAction = "<a href=\"$linkOnUserAction\">$nameAction</a>"

                val tweet = twitterClientService.getTweetById(typeCommand.tweetId)
                val tweetAuthor = twitterClientService.getAuthorForTweet(tweet)
                val tweetAuthorLink = twitterClientService.urlUser(tweetAuthor)
                val htmlAuthor = "<a href=\"$tweetAuthorLink\">$tweetAuthor</a>"

                val tweetLink = twitterClientService.getLinkOnTweet(typeCommand.tweetId, tweetAuthor)
                val htmlTweet = "<a href=\"$tweetLink\">твит</a>"

                if (typeCommand.last) "Последний лайк $htmlUserNameAction на $htmlTweet от $htmlAuthor"
                else "Лайк $htmlUserNameAction на $htmlTweet от $htmlAuthor"
            }
        }

    }

    private fun headerTextForManyMedia(typeCommand: TypeCommand): String {
        return when (typeCommand) {
            is TypeCommand.Get -> {
                val nameUser = twitterClientService.nameUser(typeCommand.username)
                val author = typeCommand.author
                val link = typeCommand.link
                "Твит от $nameUser($link) by $author"
            }
            is TypeCommand.Like -> {
                val tweet = twitterClientService.getTweetById(typeCommand.tweetId)

                val nameAction = twitterClientService.nameUser(typeCommand.username)
                val linkOnUserAction = twitterClientService.urlUser(typeCommand.username)

                val tweetAuthor = twitterClientService.getAuthorForTweet(tweet)
                val tweetAuthorLink = twitterClientService.urlUser(tweetAuthor)

                val tweetLink = twitterClientService.getLinkOnTweet(typeCommand.tweetId, tweetAuthor)

                if (typeCommand.last) "Последний лайк $tweetAuthor на твит(tweetLink) от $tweetAuthor"
                else "Лайк $nameAction($linkOnUserAction) на твит($tweetLink) от $tweetAuthor($tweetAuthorLink)"
            }
        }
    }
}
