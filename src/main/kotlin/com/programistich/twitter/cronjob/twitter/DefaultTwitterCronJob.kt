package com.programistich.twitter.cronjob.twitter

import com.programistich.twitter.common.TypeCommand
import com.programistich.twitter.model.TwitterUser
import com.programistich.twitter.service.db.DefaultDatabaseTelegramChatService
import com.programistich.twitter.service.db.DefaultDatabaseTwitterUserService
import com.programistich.twitter.service.telegram.DefaultTelegramExecutorService
import com.programistich.twitter.service.twitter.DefaultTwitterClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import twitter4j.Tweet

@Service
@Transactional
class DefaultTwitterCronJob(
    private val defaultDatabaseTwitterUserService: DefaultDatabaseTwitterUserService,
    private val databaseTelegramChatService: DefaultDatabaseTelegramChatService,
    private val defaultTwitterClientService: DefaultTwitterClientService,
    private val telegramExecutorService: DefaultTelegramExecutorService
) : TwitterCronJob {

    private val logger = LoggerFactory.getLogger(DefaultTwitterCronJob::class.java)

    //@Scheduled(fixedDelay = 1000_00)
    private fun updateTwitter() {
        updateTwitterLikesForAllUsernames()
    }

    override fun updateTwitterLikesForAllUsernames() {
        logger.info("Start updater likes")
        val usernames = defaultDatabaseTwitterUserService.getAllUsername()
        usernames.forEach {
            updateTwitterLikeForUsername(it)
        }
        logger.info("End updater likes")
    }

    fun updateTwitterLikeForUsername(username: String) {
        val existUsername = defaultDatabaseTwitterUserService.existUser(username)
        if (!existUsername) {
            logger.info("Username = $username not found in db")
            return
        }

        val tweetInDB = defaultDatabaseTwitterUserService.getTwitterUserByUsername(username)
        val tweetInTwitter: Tweet = defaultTwitterClientService.lastLikeTweetByUsername(username)

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
            val parsedTweet = defaultTwitterClientService.parseTweet(tweetInTwitter.id)
            val chats = databaseTelegramChatService.getChatsByUsername(username)
            chats.map {
                logger.info("Send tweet to $it")
                val typeTweet = TypeCommand.Like(username, tweetInTwitter.id)
                telegramExecutorService.sendTweet(it, parsedTweet, typeTweet)
            }
        }
    }
}