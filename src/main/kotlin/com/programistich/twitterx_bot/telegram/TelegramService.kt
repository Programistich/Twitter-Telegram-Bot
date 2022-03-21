package com.programistich.twitterx_bot.telegram

import com.programistich.twitterx_bot.common.Extensions.id
import com.programistich.twitterx_bot.repository.Repository
import com.programistich.twitterx_bot.template.Template
import com.programistich.twitterx_bot.template.TemplateReader
import com.programistich.twitterx_bot.twitter.TwitterClient
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class TelegramService(
    private val bot: TelegramBotExecutor,
    private val templateReader: TemplateReader,
    private val repository: Repository,
    private val twitter: TwitterClient
) {

    fun startMessage(update: Update) {
        val chatId = update.id()
        repository.addNewChat(
            chatId = chatId,
            onSuccessful = {
                bot.sendTextMessage(
                    chatId = chatId,
                    message = templateReader.getTemplate(template = Template.START),
                )
            },
            onError = {
                bot.sendTextMessage(
                    chatId = chatId,
                    message = templateReader.getTemplate(template = Template.ERROR),
                )
            },
        )
    }

    fun helpMessage(update: Update) {
        val text = templateReader.getTemplate(template = Template.HELP)
        bot.sendTextMessage(
            chatId = update.id(),
            message = text,
        )
    }

    fun pingMessage(update: Update) {
        val text = templateReader.getTemplate(template = Template.PING)
        bot.sendTextMessage(
            chatId = update.id(),
            message = text,
        )
    }

    fun donateMessage(update: Update) {
        val chatId = update.id()
        val text = templateReader.getTemplate(template = Template.DONATE)
        bot.sendTextMessage(
            chatId = chatId,
            message = text
        )
        bot.sendStickerMessage(
            chatId = chatId,
            stickerId = "CAACAgUAAxkBAAIC42HDbkEHhXo4h-g1rQoFfxqdYVjeAAIKAQACdflYFOEIo5rse7wLIwQ",
        )
    }

    fun addTwitterAccount(update: Update, username: String) {
        val chatId = update.id()
        val replyID = update.message.messageId
        repository.addTwitterAccount(
            username = username,
            chatId = chatId,
            onSuccessful = {
                bot.sendTextMessage(
                    chatId = chatId,
                    replyId = replyID,
                    message = templateReader.getTemplate(template = Template.NEW_ACCOUNT_GOOD),
                )
            },
            onExist = {
                bot.sendTextMessage(
                    chatId = chatId,
                    replyId = replyID,
                    message = templateReader.getTemplate(template = Template.NEW_ACCOUNT_BAD),
                )
            },
            onError = {
                bot.sendTextMessage(
                    chatId = chatId,
                    message = templateReader.getTemplate(template = Template.ERROR),
                )
            },
        )
    }

    fun getTweet(update: Update, tweetId: Long) {
        bot.sendTweet(
            tweet = twitter.parseTweet(tweetId),
            chatId = update.id()
        )
    }

}
