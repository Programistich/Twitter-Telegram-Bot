package com.programistich.twitter.service.telegram

import com.programistich.twitter.common.TypeByTweet
import com.programistich.twitter.common.TypeMessage
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
class TelegramBotExecutorService(
    private val bot: Bot,
    private val twitterClientService: TwitterClientService,
    private val translateService: TranslateService
) : DefaultTelegramBotExecutorService {

    private val logger = LoggerFactory.getLogger(TelegramBotExecutorService::class.java)

    override fun sendTweet(
        chatId: String,
        parsedTweet: TypeMessage?,
        typeByTweet: TypeByTweet,
        replyToMessageId: Int?,
    ) {
        val additionalText = createAdditionalText(typeByTweet)
        when (parsedTweet) {
            is TypeMessage.TextMessage -> {
                val message = SendMessage()
                message.text = formatText(additionalText, parsedTweet.text)
                message.chatId = chatId
                message.parseMode = "html"
                if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
                message.disableWebPagePreview = true
                bot.execute(message)
            }
            is TypeMessage.PhotoMessage -> {
                val message = SendPhoto()
                message.chatId = chatId
                message.caption = formatText(additionalText, parsedTweet.caption)
                message.photo = InputFile(parsedTweet.url)
                message.parseMode = "html"
                if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
                bot.execute(message)
            }
            is TypeMessage.AnimatedMessage -> {
                val message = SendAnimation()
                message.chatId = chatId
                message.animation = InputFile(parsedTweet.url)
                message.parseMode = "html"
                if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
                message.caption = formatText(additionalText, parsedTweet.caption)
                bot.execute(message)
            }
            is TypeMessage.VideoMessage -> {
                val message = SendVideo()
                message.chatId = chatId
                message.parseMode = "html"
                message.caption = formatText(additionalText, parsedTweet.caption)
                message.video = InputFile(parsedTweet.url)
                if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
                bot.execute(message)
            }
            is TypeMessage.ManyMediaMessage -> {
                val medias = parsedTweet.urlsMedia
                val message = SendMediaGroup()
                message.chatId = chatId
                val listMedia = arrayListOf<InputMedia>()
                var media = InputMediaPhoto()
                media.media = medias[0]
                if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
                media.caption = formatText(additionalText, parsedTweet.caption)
                listMedia.add(media)
                for (i in 1 until medias.size) {
                    media = InputMediaPhoto()
                    media.media = medias[i]
                    listMedia.add(media)
                }
                message.medias = listMedia
                bot.execute(message)

            }
            else -> {
                bot.execute(SendMessage(chatId, "Что-то пошло не так"))
            }
        }
    }

    override fun sendTextMessage(chatId: String, text: String, replyToMessageId: Int?) {
        val message = SendMessage()
        message.text = text
        message.chatId = chatId
        message.parseMode = "html"
        if (replyToMessageId != null) message.replyToMessageId = replyToMessageId
        message.disableWebPagePreview = true
        bot.execute(message)
    }

    override fun deleteMessage(chatId: String, messageId: Int) {
        try {
            val delete = DeleteMessage()
            delete.chatId = chatId
            delete.messageId = messageId
            bot.execute(delete)
        } catch (e: TelegramApiException) {
            logger.info("Cant delete msg")
        }

    }

    private fun formatText(additionalText: String, textTweet: String): String {
        val translateText = translateService.translate(textTweet.trim())
        val formatUsername = twitterClientService.usernameToLink(translateText)
        return additionalText + formatUsername
    }

    private fun createAdditionalText(typeByTweet: TypeByTweet): String {
        when (typeByTweet) {
            is TypeByTweet.Get -> {
                val nameUser = twitterClientService.nameUser(typeByTweet.username)
                val linkUser = twitterClientService.urlUser(typeByTweet.username)
                val author = typeByTweet.author
                val link = typeByTweet.link
                return "Твит от <a href=\"$linkUser\">$nameUser</a> по <a href=\"$link\">ссылке</a> by $author\n\n"
            }
            is TypeByTweet.Like -> {
                val nameUser = twitterClientService.nameUser(typeByTweet.username)
                val linkUser = twitterClientService.urlUser(typeByTweet.username)
                val tweet = twitterClientService.getTweetById(typeByTweet.tweetId)
                val tweetAuthor = twitterClientService.getAuthorForTweet(tweet)
                val tweetLink = twitterClientService.getLinkOnTweet(typeByTweet.tweetId, tweetAuthor)
                val userCollect = "<a href=\"$linkUser\">$nameUser</a>"
                if (typeByTweet.last) return "Последний <a href=\"$tweetLink\">лайк</a> от $userCollect\n\n"
                else return "Новый <a href=\"$tweetLink\">лайк</a> от $userCollect\n\n"
            }
        }

    }
}
