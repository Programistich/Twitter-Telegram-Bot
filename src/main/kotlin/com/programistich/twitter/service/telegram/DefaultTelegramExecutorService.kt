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
import twitter4j.Tweet

@Service
class DefaultTelegramExecutorService(
    private val bot: Bot,
    private val twitterClientService: TwitterClientService,
    private val translateService: TranslateService,
) : TelegramExecutorService {

    private val logger = LoggerFactory.getLogger(DefaultTelegramExecutorService::class.java)

    fun Tweet.newId(): Long? {
        val ids = listOf(
            quotedTweetId,
            retweetId,
            repliedToTweetId)
        return ids.filterNotNull().firstOrNull()
    }

    override fun sendTweetEntryPoint(tweetId: Long, chatId: String, author: String?, isNew: Boolean) {
        val username = twitterClientService.getUserNameByTweetId(tweetId)
        val newMessageId = sendTweet(tweetId, chatId, null)
        sendTweet(
            chatId = chatId,
            typeMessage = twitterClientService.parseTweet(tweetId),
            typeCommand = TypeCommand.Tweet(username, tweetId, author, isNew),
            replyToMessageId = newMessageId
        )
    }

    private fun sendTweet(tweetId: Long, chatId: String, messageId: Int?): Int? {
        val tweet = twitterClientService.getTweetById(tweetId)
        val id = tweet.newId()
        if (id != null) {
            val newMessageId = sendTweet(id, chatId, messageId)
            val username = twitterClientService.getUserNameByTweetId(id)
            return sendTweet(
                chatId = chatId,
                typeMessage = twitterClientService.parseTweet(id),
                typeCommand = TypeCommand.Tweet(username, id),
                replyToMessageId = newMessageId
            )
        }
        return messageId
    }

    override fun sendTweet(
        chatId: String,
        typeMessage: TypeMessageTelegram?,
        typeCommand: TypeCommand,
        replyToMessageId: Int?,
    ): Int {
        val headerText = headerText(typeCommand)
        return when (typeMessage) {
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
                val textMessage = formatText(headerText(typeCommand), typeMessage.text)
                sendManyMediaMessageByUrls(chatId, textMessage, typeMessage.urlsMedia, replyToMessageId)
            }
            else -> {
                bot.execute(SendMessage(chatId, "Что-то пошло не так")).messageId
            }
        }
    }

    private fun formatText(additionalText: String, textTweet: String): String {
        val translateText = translateService.translateText(textTweet.trim())
        val formatUsername = twitterClientService.usernameToLink(translateText)
        return additionalText + "\n\n" + formatUsername
    }

    override fun sendTextMessage(chatId: String, text: String, replyToMessageId: Int?): Int {
        val message = SendMessage()
        message.text = text
        message.chatId = chatId
        message.parseMode = "html"
        message.disableWebPagePreview = true
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        message.disableWebPagePreview = true
        return bot.execute(message).messageId
    }

    override fun sendPhotoMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?): Int {
        val message = SendPhoto()
        message.chatId = chatId
        message.caption = textMessage
        message.photo = InputFile(url)
        message.parseMode = "html"
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        return bot.execute(message).messageId
    }

    override fun sendAnimatedMessageByUrl(
        chatId: String,
        textMessage: String,
        url: String,
        replyToMessageId: Int?,
    ): Int {
        val message = SendAnimation()
        message.chatId = chatId
        message.caption = textMessage
        message.animation = InputFile(url)
        message.parseMode = "html"
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        return bot.execute(message).messageId
    }

    override fun sendVideoMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?): Int {
        val message = SendVideo()
        message.chatId = chatId
        message.caption = textMessage
        message.video = InputFile(url)
        message.parseMode = "html"
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        return bot.execute(message).messageId
    }

    override fun sendManyMediaMessageByUrls(
        chatId: String,
        textMessage: String,
        urls: List<String>,
        replyToMessageId: Int?,
    ): Int {
        val message = SendMediaGroup()
        message.chatId = chatId
        val listMedia = arrayListOf<InputMedia>()
        var media = InputMediaPhoto()
        media.media = urls[0]
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        media.caption = textMessage
        media.parseMode = "html"
        listMedia.add(media)
        for (i in 1 until urls.size) {
            media = InputMediaPhoto()
            media.media = urls[i]
            listMedia.add(media)
        }
        message.medias = listMedia
        return bot.execute(message).map { it.messageId }.first()
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
            is TypeCommand.Tweet -> {
                val tweet = twitterClientService.getTweetById(typeCommand.tweetId)
                val tweetAuthor = twitterClientService.getAuthorForTweet(tweet)
                val tweetLink = twitterClientService.getLinkOnTweet(typeCommand.tweetId, tweetAuthor)
                val tweetHtml = "<a href=\"$tweetLink\">Твит</a>"
                val tweetAuthorLink = twitterClientService.urlUser(tweetAuthor)
                val htmlAuthor = "<a href=\"$tweetAuthorLink\">$tweetAuthor</a>"
                var text = "$tweetHtml от $htmlAuthor"
                if (typeCommand.author != null) text += " by ${typeCommand.author}"
                if(typeCommand.last) text = "Последний $text"
                return text
            }
        }

    }
}
