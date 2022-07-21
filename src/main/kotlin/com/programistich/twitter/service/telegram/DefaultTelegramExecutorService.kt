package com.programistich.twitter.service.telegram

import com.programistich.twitter.cache.TelegramCache
import com.programistich.twitter.service.twitter.InternalTweet
import com.programistich.twitter.service.twitter.TwitterService
import com.programistich.twitter.telegram.TelegramBotInstance
import com.programistich.twitter.telegram.TelegramMessageType
import com.programistich.twitter.template.Template
import com.programistich.twitter.template.TemplateReader
import com.programistich.twitter.translate.TranslateService
import com.programistich.twitter.utils.TypeCommand
import com.programistich.twitter.utils.UnShortenerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.media.InputMedia
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import twitter4j.Tweet

@Service
class DefaultTelegramExecutorService(
    private val telegramBotInstance: TelegramBotInstance,
    private val twitterService: TwitterService,
    private val translateService: TranslateService,
    private val shortenerService: UnShortenerService,
    private val template: TemplateReader,
    private val cache: TelegramCache
) : TelegramExecutorService {

    private val logger = LoggerFactory.getLogger(DefaultTelegramExecutorService::class.java)

    fun Tweet.newId(): Long? {
        val ids = listOf(
            quotedTweetId,
            retweetId,
            repliedToTweetId
        )
        return ids.filterNotNull().firstOrNull()
    }

    override fun sendTweetEntryPoint(tweetId: Long, chatId: String, author: String?, isNew: Boolean, replyMessage: Int?) {
        val newMessageId = sendTweet(tweetId, chatId, replyMessage)
        sendTweet(
            chatId = chatId,
            typeMessage = twitterService.parseTweet(tweetId),
            typeCommand = if (author == null) TypeCommand.Tweet(tweetId, isNew) else TypeCommand.Get(tweetId, author),
            replyToMessageId = newMessageId
        )
    }

    override fun sendTweetEntryPoint(tweetInternal: InternalTweet, chatId: String, author: String?, isNew: Boolean, replyMessage: Int?) {
        sendTweet(tweetInternal, chatId, replyMessage, author)
    }

    private fun sendTweet(tweetInternal: InternalTweet, chatId: String, messageId: Int?, author: String?): Int? {
        var newMessageId: Int? = messageId
        if (tweetInternal.nextTweet != null) {
            write(chatId)
            newMessageId = sendTweet(tweetInternal.nextTweet, chatId, messageId, null)
        }
        write(chatId)
        if (author == null) {
            return sendTweet(
                chatId = chatId,
                typeMessage = tweetInternal.current.content,
                typeCommand = TypeCommand.Tweet(tweetInternal.current.id),
                replyToMessageId = newMessageId
            )
        }
        else {
            val existTweet = cache.get(tweetInternal.current.id, chatId)
            return if (existTweet == null) sendTweet(
                chatId = chatId,
                typeMessage = tweetInternal.current.content,
                typeCommand = TypeCommand.Get(
                    tweetId = tweetInternal.current.id,
                    author = author
                ),
                replyToMessageId = newMessageId
            )
            else {
                val link = twitterService.getLinkOnTweet(tweetInternal.current.id, tweetInternal.current.author.username)
                val text = template.getTemplate(template = Template.TWEET_EXIST, values = arrayOf(link, author))
                sendTextMessage(chatId = chatId, text = text, replyToMessageId = existTweet)
            }
        }
    }

    private fun sendTweet(tweetId: Long, chatId: String, messageId: Int?): Int? {
        val tweet = twitterService.getTweetById(tweetId)
        val id = tweet.newId()
        if (id != null) {
            val newMessageId = sendTweet(id, chatId, messageId)
            return sendTweet(
                chatId = chatId,
                typeMessage = twitterService.parseTweet(id),
                typeCommand = TypeCommand.Tweet(id),
                replyToMessageId = newMessageId
            )
        }
        return messageId
    }

    override fun sendTweet(
        chatId: String,
        typeMessage: TelegramMessageType?,
        typeCommand: TypeCommand,
        replyToMessageId: Int?
    ): Int {
        if (typeCommand !is TypeCommand.Like) {
            val existId = cache.get(typeCommand.tweetId, chatId)
            if (existId != null) return existId
        }
        val headerText = headerText(typeCommand)
        val id = when (typeMessage) {
            is TelegramMessageType.TextMessage -> {
                val textMessage = formatText(headerText, typeMessage.text)
                sendTextMessage(chatId, textMessage, replyToMessageId)
            }
            is TelegramMessageType.PhotoMessage -> {
                val textMessage = formatText(headerText, typeMessage.text)
                sendPhotoMessageByUrl(chatId, textMessage, typeMessage.url, replyToMessageId)
            }
            is TelegramMessageType.AnimatedMessage -> {
                val textMessage = formatText(headerText, typeMessage.text)
                sendAnimatedMessageByUrl(chatId, textMessage, typeMessage.url, replyToMessageId)
            }
            is TelegramMessageType.VideoMessage -> {
                val textMessage = formatText(headerText, typeMessage.text)
                sendVideoMessageByUrl(chatId, textMessage, typeMessage.url, replyToMessageId, typeCommand.tweetId)
            }
            is TelegramMessageType.ManyMediaMessage -> {
                val textMessage = formatText(headerText(typeCommand), typeMessage.text)
                sendManyMediaMessageByUrls(chatId, textMessage, typeMessage.urlsMedia, replyToMessageId)
            }
            else -> {
                telegramBotInstance.execute(SendMessage(chatId, "Что-то пошло не так")).messageId
            }
        }
        if (typeCommand !is TypeCommand.Like) {
            return cache.add(typeCommand.tweetId, chatId, id)
        }
        return id
    }

    private fun getAllLinks(text: String): Map<String, String> {
        val links = mutableMapOf<String, String>()
        text.split("\\s".toRegex()).forEach {
            if (it.startsWith("https://t.co/")) {
                links[it] = shortenerService.shortLink(it)
            }
        }
        return links
    } private fun formatText(additionalText: String, textTweet: String): String {
        var parseLink = textTweet
        val links = getAllLinks(textTweet)
        links.forEach { (linkTwitter, link) ->
            parseLink = parseLink.replace(linkTwitter, link)
        }
        val translateText = translateService.translateText(parseLink.trim())
        val formatUsername = twitterService.usernameToLink(translateText)
        return additionalText + "\n\n" + formatUsername
    }

    override fun sendTextMessage(chatId: String, text: String, replyToMessageId: Int?): Int {
        return try {
            val message = SendMessage()
            message.text = text
            message.chatId = chatId
            message.parseMode = "html"
            message.disableWebPagePreview = true
            if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
            message.disableWebPagePreview = true
            return telegramBotInstance.execute(message).messageId
        } catch (e: TelegramApiRequestException) {
            logger.info("Exception with exc $e")
            0
        }
    }

    override fun sendPhotoMessageByUrl(chatId: String, textMessage: String, url: String, replyToMessageId: Int?): Int {
        return try {
            val message = SendPhoto()
            message.chatId = chatId
            message.caption = textMessage
            message.photo = InputFile(url)
            message.parseMode = "html"
            if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
            return telegramBotInstance.execute(message).messageId
        } catch (e: TelegramApiRequestException) {
            logger.info("Exception with exc $e")
            0
        }
    }

    override fun sendAnimatedMessageByUrl(
        chatId: String,
        textMessage: String,
        url: String,
        replyToMessageId: Int?
    ): Int {
        return try {
            val message = SendAnimation()
            message.chatId = chatId
            message.caption = textMessage
            message.animation = InputFile(url)
            message.parseMode = "html"
            if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
            telegramBotInstance.execute(message).messageId
        } catch (e: TelegramApiRequestException) {
            logger.info("Exception with exc $e")
            0
        }
    }

    override fun sendVideoMessageByUrl(
        chatId: String,
        textMessage: String,
        url: List<String>,
        replyToMessageId: Int?,
        tweetId: Long
    ): Int {
        for (i in 0..url.size - 2) {
            val message = SendVideo()
            message.chatId = chatId
            message.caption = textMessage
            message.video = InputFile(url[i])
            message.parseMode = "html"
            if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
            try {
                return telegramBotInstance.execute(message).messageId
            } catch (_: TelegramApiException) {
            }
        }
        val message = SendVideo()
        message.chatId = chatId
        message.caption = textMessage
        message.video = InputFile(url.last())
        message.parseMode = "html"
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        return try {
            telegramBotInstance.execute(message).messageId
        } catch (exception: TelegramApiException) {
            logger.info("Exception with $url exc $exception")
            val link = twitterService.parseTweetForTelegram(tweetId).url
            val text = template.getTemplate(
                template = Template.ERROR,
                values = arrayOf(link)
            )
            sendTextMessage(
                chatId = chatId,
                text = text,
                replyToMessageId = replyToMessageId
            )
        }
    }

    override fun sendManyMediaMessageByUrls(
        chatId: String,
        textMessage: String,
        urls: List<String>,
        replyToMessageId: Int?
    ): Int {
        return try {
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
            return telegramBotInstance.execute(message).map { it.messageId }.first()
        } catch (e: TelegramApiRequestException) {
            logger.info("Exception with exc $e")
            0
        }
    }

    override fun sendStickerMessage(chatId: String, stickerId: String) {
        try {
            val message = SendSticker()
            message.chatId = chatId
            message.sticker = InputFile(stickerId)
            telegramBotInstance.execute(message)
            logger.info("Send sticker message to $chatId")
        } catch (e: TelegramApiRequestException) {
            logger.info("Exception with $stickerId exc $e")
        }
    }

    override fun deleteMessage(chatId: String, messageId: Int) {
        try {
            val delete = DeleteMessage()
            delete.chatId = chatId
            delete.messageId = messageId
            telegramBotInstance.execute(delete)
            logger.info("Delete message in $chatId")
        } catch (e: TelegramApiException) {
            logger.info("Cant delete msg")
        }
    }

    override fun write(chatId: String) {
        try {
            val message = SendChatAction()
            message.chatId = chatId
            message.setAction(ActionType.TYPING)
            telegramBotInstance.execute(message)
        } catch (e: TelegramApiException) {
            logger.info("Cant action msg")
        }
    }

    private fun headerText(typeCommand: TypeCommand): String {
        val tweet = twitterService.parseTweetForTelegram(typeCommand.tweetId)
        return when (typeCommand) {
            is TypeCommand.Get -> {
                val htmlLinkAuthor = tweet.author.html()
                val htmlLinkTweet = tweet.html("Твит")
                val telegramUser = typeCommand.author
                "$htmlLinkTweet от $htmlLinkAuthor by $telegramUser"
            }
            is TypeCommand.Like -> {
                val htmlLinkAuthor = tweet.author.html()
                val htmlLinkTweet = tweet.html("твит")
                val linkWhoLiked = twitterService.urlUser(typeCommand.whoLiked)
                val nameWhoLiked = twitterService.nameUser(typeCommand.whoLiked)
                val htmlLinkWhoLiked = "<a href=\"$linkWhoLiked\">$nameWhoLiked</a>"

                if (typeCommand.last) "Последний лайк $htmlLinkWhoLiked на $htmlLinkTweet от $htmlLinkAuthor"
                else "Лайк $htmlLinkWhoLiked на $htmlLinkTweet от $htmlLinkAuthor"
            }
            is TypeCommand.Tweet -> {
                val htmlLinkAuthor = tweet.author.html()
                if (typeCommand.last) {
                    val htmlLinkTweet = tweet.html("Последний твит")
                    "$htmlLinkTweet от $htmlLinkAuthor"
                } else {
                    val htmlLinkTweet = tweet.html("Твит")
                    "$htmlLinkTweet от $htmlLinkAuthor"
                }
            }
        }
    }
}
