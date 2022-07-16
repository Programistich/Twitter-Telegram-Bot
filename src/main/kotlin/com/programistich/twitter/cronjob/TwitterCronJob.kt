package com.programistich.twitter.cronjob

import com.programistich.twitter.entity.TwitterUser
import com.programistich.twitter.service.db.DefaultDatabaseTelegramChatService
import com.programistich.twitter.service.db.DefaultDatabaseTwitterUserService
import com.programistich.twitter.service.telegram.DefaultTelegramExecutorService
import com.programistich.twitter.service.twitter.TwitterService
import com.programistich.twitter.utils.TypeCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import twitter4j.Tweet
import java.util.concurrent.TimeUnit

@Service
@Transactional
class TwitterCronJob(
    private val defaultDatabaseTwitterUserService: DefaultDatabaseTwitterUserService,
    private val databaseTelegramChatService: DefaultDatabaseTelegramChatService,
    private val twitterService: TwitterService,
    private val telegramExecutorService: DefaultTelegramExecutorService,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    @Scheduled(fixedDelay = 2 * 60 * 1000)
    fun updateTwitter() {
        updateTwitterForAllUsernames()
    }

    private fun updateTwitterForAllUsernames() {
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
            TimeUnit.SECONDS.sleep(2)
            updateTweetForUsername(it, tweetInDB)
            TimeUnit.SECONDS.sleep(5)
        }
        logger.info("End update twitter accounts")
    }

    private fun updateTweetForUsername(username: String, tweetInDB: TwitterUser?) {
        logger.info("Update tweet twitter account $username")
        val tweetInTwitter: Tweet = twitterService.lastTweetByUsername(username)
        if (tweetInDB != null && tweetInTwitter.id > (tweetInDB.lastTweetId ?: 0)) {
            logger.info("New tweet from $username id = ${tweetInTwitter.id}")
            val twitterUser = TwitterUser(
                username = username,
                lastLikeId = tweetInDB.lastLikeId,
                lastTweetId = tweetInTwitter.id,
                chats = tweetInDB.chats
            )
            defaultDatabaseTwitterUserService.updateTwitterUser(twitterUser)
            logger.info("Update username = $username")
            val chats = databaseTelegramChatService.getChatsByUsername(username)
            val internalTweet = twitterService.parseInternalTweet(tweetInTwitter.id)
            chats.map {
                if (it.isChannel && tweetInTwitter.retweetId != null) return
                else {
                    logger.info("Send tweet to ${it.chatId}")
                    telegramExecutorService.sendTweetEntryPoint(internalTweet, it.chatId)
                }
            }
        }
    }

    private fun updateLikeForUsername(username: String, tweetInDB: TwitterUser?) {
        logger.info("Update like twitter account $username")
        val tweetInTwitter: Tweet = twitterService.lastLikeByUsername(username)
        if (tweetInDB == null || tweetInTwitter.id != tweetInDB.lastLikeId) {
            logger.info("New like from $username id = $tweetInTwitter.id")
            val twitterUser = TwitterUser(
                username = username,
                lastLikeId = tweetInTwitter.id,
                lastTweetId = tweetInDB!!.lastTweetId,
                chats = tweetInDB.chats
            )
            defaultDatabaseTwitterUserService.updateTwitterUser(twitterUser)
            logger.info("Update username = $username")
            val parsedTweet = twitterService.parseTweet(tweetInTwitter.id)
            val chats = databaseTelegramChatService.getChatsByUsername(username)
            chats.filter { !it.isChannel }.map {
                logger.info("Send like tweet to $it")
                val typeTweet = TypeCommand.Like(username, tweetInTwitter.id)
                telegramExecutorService.sendTweet(it.chatId, parsedTweet, typeTweet)
            }
        }
    }
}
