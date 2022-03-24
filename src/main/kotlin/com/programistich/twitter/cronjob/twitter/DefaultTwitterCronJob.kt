package com.programistich.twitter.cronjob.twitter

import com.programistich.twitter.common.TypeCommand
import com.programistich.twitter.entity.TwitterUser
import com.programistich.twitter.service.db.DefaultDatabaseTelegramChatService
import com.programistich.twitter.service.db.DefaultDatabaseTwitterUserService
import com.programistich.twitter.service.telegram.DefaultTelegramExecutorService
import com.programistich.twitter.service.twitter.TwitterClientService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import twitter4j.Tweet

@Service
@Transactional
class DefaultTwitterCronJob(
    private val defaultDatabaseTwitterUserService: DefaultDatabaseTwitterUserService,
    private val databaseTelegramChatService: DefaultDatabaseTelegramChatService,
    private val twitterClientService: TwitterClientService,
    private val telegramExecutorService: DefaultTelegramExecutorService,
) : TwitterCronJob {

    private val logger = LoggerFactory.getLogger(DefaultTwitterCronJob::class.java)

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    fun updateTwitter() {
        updateTwitterForAllUsernames()
    }

    override fun updateTwitterForAllUsernames() {
        logger.info("Start update twitter accounts")
        val usernames = defaultDatabaseTwitterUserService.getAllUsername()
        usernames.forEach {
            val existUsername = defaultDatabaseTwitterUserService.existUser(it)
            if (!existUsername) {
                logger.info("Username = $it not found in db")
                return
            }
            val tweetInDB = defaultDatabaseTwitterUserService.getTwitterUserByUsername(it)
            updateLikeForUsername(it, tweetInDB)
            updateTweetForUsername(it, tweetInDB)
        }
        logger.info("End update twitter accounts")
    }

    private fun updateTweetForUsername(username: String, tweetInDB: TwitterUser?) {
        val tweetInTwitter: Tweet = twitterClientService.lastTweetByUsername(username)
        if (tweetInDB == null || tweetInTwitter.id != tweetInDB.lastTweetId) {
            logger.info("New tweet from $username id = $tweetInTwitter.id")
            val twitterUser = TwitterUser(
                username = username,
                lastLikeId = tweetInDB!!.lastLikeId,
                lastTweetId = tweetInTwitter.id,
                chats = tweetInDB.chats
            )
            defaultDatabaseTwitterUserService.updateTwitterUser(twitterUser)
            logger.info("Update username = $username")
            val chats = databaseTelegramChatService.getChatsByUsername(username)
            chats.map {
                logger.info("Send tweet to $it")
                telegramExecutorService.sendTweetEntryPoint(tweetInTwitter.id, it)
            }
        }
    }


    private fun updateLikeForUsername(username: String, tweetInDB: TwitterUser?) {
        val tweetInTwitter: Tweet = twitterClientService.lastLikeByUsername(username)
        if (tweetInDB == null || tweetInTwitter.id != tweetInDB.lastLikeId) {
            logger.info("New tweet from $username id = $tweetInTwitter.id")
            val twitterUser = TwitterUser(
                username = username,
                lastLikeId = tweetInTwitter.id,
                lastTweetId = tweetInDB!!.lastTweetId,
                chats = tweetInDB.chats
            )
            defaultDatabaseTwitterUserService.updateTwitterUser(twitterUser)
            logger.info("Update username = $username")
            val parsedTweet = twitterClientService.parseTweet(tweetInTwitter.id)
            val chats = databaseTelegramChatService.getChatsByUsername(username)
            chats.map {
                logger.info("Send tweet to $it")
                val typeTweet = TypeCommand.Like(username, tweetInTwitter.id)
                telegramExecutorService.sendTweet(it, parsedTweet, typeTweet)
            }
        }
    }
}
