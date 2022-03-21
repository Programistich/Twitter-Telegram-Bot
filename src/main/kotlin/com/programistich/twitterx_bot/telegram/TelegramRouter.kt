package com.programistich.twitterx_bot.telegram

import com.programistich.twitterx_bot.common.Command.*
import com.programistich.twitterx_bot.common.Extensions.getCommand
import com.programistich.twitterx_bot.common.Extensions.textMessage
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import twitter4j.Twitter

@Component
class TelegramRouter(
    private val configuration: TelegramBotConfiguration,
    private val telegram: TelegramService,
    private val twitter: Twitter,
) {

    fun obtainUpdate(update: Update) {
        update.textMessage()?.let { message ->
            update.getCommand(configuration.username)?.let {
                val messageWithoutCommand = message.replace(it.command, "")
                when (it) {
                    START -> telegram.startMessage(update)
                    HELP -> telegram.helpMessage(update)
                    PING -> telegram.pingMessage(update)
                    DONATE -> telegram.donateMessage(update)
                    GET -> isTwitterAccount(messageWithoutCommand) { username ->
                        telegram.addTwitterAccount(update = update, username = username)
                    }
                    NEW -> isTweet(messageWithoutCommand) { tweetId ->
                        telegram.getTweet(update = update, tweetId = tweetId)
                    }
                    else -> {}
                }
            }

            isTwitterAccount(message) { username ->
                telegram.addTwitterAccount(update = update, username = username)
            }
            isTweet(message) { tweetId ->
                telegram.getTweet(update = update, tweetId = tweetId)
            }
        }
    }

    private fun isTwitterAccount(message: String, onAction: (String) -> Unit) {
        val nameAccount = message
            .replace(".*twitter.com/".toRegex(), "")
            .replace("\\W.*".toRegex(), "")
        runCatching {
            twitter.showUser(nameAccount)
            onAction.invoke(nameAccount)
        }
    }

    private fun isTweet(message: String, onAction: (Long) -> Unit) {
        val idTweet = message
            .replace(".*status/".toRegex(), "")
            .replace("\\W.*".toRegex(), "")
            .replace("\\D.*".toRegex(), "")
        runCatching {
            twitter.showStatus(idTweet.toLong())
            onAction.invoke(idTweet.toLong())
        }
    }
}

