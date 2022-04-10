package com.programistich.twitter.telegram

import com.programistich.twitter.utils.Extensions.id
import com.programistich.twitter.utils.TypeCommand
import com.programistich.twitter.repository.Repository
import com.programistich.twitter.service.telegram.TelegramExecutorService
import com.programistich.twitter.service.twitter.TwitterService
import com.programistich.twitter.stocks.StocksService
import com.programistich.twitter.template.Template
import com.programistich.twitter.template.TemplateReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.Update

@Service
@Transactional
class TelegramBotService(
    private val twitterService: TwitterService,
    private val bot: TelegramExecutorService,
    private val stocksService: StocksService,
    private val repository: Repository,
    private val template: TemplateReader,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun startCommand(update: Update) {
        val chatId = update.id()
        val text = template.getTemplate(template = Template.START)
        repository.addTelegramChat(chatId)
        logger.info("Start command by $chatId")
        bot.sendTextMessage(
            chatId = chatId,
            text = text
        )
    }

    fun helpCommand(update: Update) {
        val chatId = update.id()
        val text = template.getTemplate(template = Template.HELP)
        logger.info("Help command by $chatId")
        bot.sendTextMessage(
            chatId = chatId,
            text = text
        )
    }

    fun pingCommand(update: Update) {
        val chatId = update.id()
        val text = template.getTemplate(template = Template.PING)
        logger.info("Ping command by $chatId")
        bot.sendTextMessage(
            chatId = chatId,
            text = text
        )
    }

    fun donateCommand(update: Update) {
        val chatId = update.id()
        val text = template.getTemplate(template = Template.DONATE)
        logger.info("Donate command by $chatId")
        bot.sendTextMessage(
            chatId = chatId,
            text = text
        )
        bot.sendStickerMessage(
            chatId = chatId,
            stickerId = "CAACAgUAAxkBAAIC42HDbkEHhXo4h-g1rQoFfxqdYVjeAAIKAQACdflYFOEIo5rse7wLIwQ"
        )
    }

    fun addTwitterAccountCommand(update: Update, username: String) {
        val chatId = update.id()
        logger.info("Add command by $chatId")
        val existUsername = twitterService.existUsernameInTwitter(username)
        if (existUsername) {
            logger.info("Username $username exist in twitter")
            val existAccountInChat = repository.addTwitterAccount(chatId = chatId, username = username)
            if (existAccountInChat) {
                val text = template.getTemplate(template = Template.ACCOUNT_EXIST, values = arrayOf(username))
                logger.info("Username $username exist in chat $chatId")
                bot.sendTextMessage(chatId = chatId, text = text)
            } else {
                val text = template.getTemplate(template = Template.ACCOUNT_GOOD, values = arrayOf(username))
                logger.info("Username $username not exist in chat $chatId")
                bot.sendTextMessage(chatId = chatId, text = text)
                lastLikeByUsernameCommand(update, username)
                lastTweetByUsernameCommand(update, username)
            }
        } else {
            val text = template.getTemplate(template = Template.ACCOUNT_NOT_FOUND, values = arrayOf(username))
            logger.info("Username $username not exist in twitter")
            bot.sendTextMessage(
                chatId = chatId,
                text = text
            )
        }
    }

    private fun lastLikeByUsernameCommand(update: Update, username: String) {
        val chatId = update.id()
        val twitterUser = repository.getTwitterAccountByUsername(username)
        logger.info("Last like by $twitterUser in $chatId")
        twitterUser?.let { user ->
            user.lastLikeId?.let {
                val typeMessage = twitterService.parseTweet(it)
                val typeTweet = TypeCommand.Like(username, it, true)
                bot.sendTweet(chatId, typeMessage, typeTweet)
            }
        }
    }

    private fun lastTweetByUsernameCommand(update: Update, username: String) {
        val chatId = update.id()
        val twitterUser = repository.getTwitterAccountByUsername(username)
        logger.info("Last tweet command by $twitterUser")
        twitterUser?.let { user ->
            user.lastTweetId?.let {
                bot.sendTweetEntryPoint(
                    tweetId = it,
                    chatId = chatId,
                    isNew = true
                )
            }
        }
    }

    fun getTweetCommand(update: Update, tweetId: Long) {
        val chatId = update.id()
        logger.info("Get tweet command by $chatId")
        val user = update.message.from.firstName
        val existUsername = twitterService.existTweetId(tweetId)
        if (existUsername) {
            logger.info("Get tweet by id $tweetId")
            bot.sendTweetEntryPoint(
                tweetId = tweetId,
                chatId = chatId,
                author = user
            )
            bot.deleteMessage(
                chatId = chatId,
                messageId = update.message.messageId
            )
        } else {
            val text = template.getTemplate(template = Template.TWEET_NOT_FOUND)
            logger.info("Tweet by id $tweetId not found")
            bot.sendTextMessage(
                chatId = chatId,
                text = text
            )
        }
    }


    fun stocksCommand(update: Update) {
        val chatId = update.id()
        val messageId = update.message.messageId
        val stock = stocksService.getStock("TSLA")
        stocksService.sendStock(chatId, messageId, stock)
    }

}
