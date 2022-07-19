package com.programistich.twitter.cronjob

import com.programistich.twitter.entity.TwitterUser
import com.programistich.twitter.service.db.DefaultDatabaseTelegramChatService
import com.programistich.twitter.service.db.DefaultDatabaseTwitterUserService
import com.programistich.twitter.service.telegram.DefaultTelegramExecutorService
import com.programistich.twitter.service.twitter.TwitterService
import com.programistich.twitter.utils.TypeCommand
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import twitter4j.Tweet
import java.util.concurrent.TimeUnit
import org.springframework.transaction.annotation.Transactional

@Service
class TwitterCronJob(
    private val defaultDatabaseTwitterUserService: DefaultDatabaseTwitterUserService,
    private val databaseTelegramChatService: DefaultDatabaseTelegramChatService,
    private val twitterService: TwitterService,
    private val telegramExecutorService: DefaultTelegramExecutorService,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedDelay = 2 * 60 * 1000)
    fun updateTwitter() {
        updateTwitterForAllUsernames()
    }

    @Transactional
    fun updateTwitterForAllUsernames() {
        logger.info("Start update twitter accounts")
        val usernames = defaultDatabaseTwitterUserService.getAllUsername().reversed()
        usernames.forEach {
            val user = defaultDatabaseTwitterUserService.getTwitterUserByUsername(it)
            if (user == null) {
                logger.info("Username = $it not found in db")
                return
            } else {
                updateLikeForUsername(it, user)
                TimeUnit.SECONDS.sleep(10)
                updateTweetForUsername(it, user)
                TimeUnit.SECONDS.sleep(10)
            }
        }
        logger.info("End update twitter accounts")
    }

    @Transactional
    fun updateTweetForUsername(username: String, user: TwitterUser) {
        logger.info("Update tweet twitter account $username")
        val tweetInTwitter: Tweet = twitterService.lastTweetByUsername(username)
        if (tweetInTwitter.id > (user.lastTweetId ?: 0)) {
            logger.info("New tweet from $username id = ${tweetInTwitter.id}")
            user.lastTweetId = tweetInTwitter.id
            defaultDatabaseTwitterUserService.updateTwitterUser(user)
            logger.info("Update username = $username")
            val internalTweet = twitterService.parseInternalTweet(tweetInTwitter.id) ?: return
            user.chats.map {
                if (it.isChannel && tweetInTwitter.retweetId != null) return
                else {
                    logger.info("Send tweet to ${it.chatId}")
                    telegramExecutorService.sendTweetEntryPoint(internalTweet, it.chatId)
                }
            }
        }
    }

    @Transactional
    fun updateLikeForUsername(username: String, user: TwitterUser) {
        logger.info("Update like twitter account $username")
        val tweetInTwitter: Tweet = twitterService.lastLikeByUsername(username)
        if (tweetInTwitter.id != user.lastLikeId) {
            logger.info("New like from $username id = $tweetInTwitter.id")
            user.lastLikeId = tweetInTwitter.id
            defaultDatabaseTwitterUserService.updateTwitterUser(user)
            logger.info("Update username = $username")
            val parsedTweet = twitterService.parseTweet(tweetInTwitter.id)
            user.chats.filter { !it.isChannel }.map {
                logger.info("Send like tweet to $it")
                val typeTweet = TypeCommand.Like(username, tweetInTwitter.id)
                telegramExecutorService.sendTweet(it.chatId, parsedTweet, typeTweet)
            }
        }
    }
}
