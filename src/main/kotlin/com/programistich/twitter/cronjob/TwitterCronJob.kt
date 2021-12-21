package com.programistich.twitter.cronjob

import com.programistich.twitter.common.TypeByTweet
import com.programistich.twitter.model.TwitterUser
import com.programistich.twitter.service.db.DatabaseTelegramChatService
import com.programistich.twitter.service.db.DatabaseTwitterUserService
import com.programistich.twitter.service.telegram.TelegramBotExecutorService
import com.programistich.twitter.service.twitter.TwitterClientService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TwitterCronJob(
    private val databaseTwitterUserService: DatabaseTwitterUserService,
    private val databaseTelegramChatService: DatabaseTelegramChatService,
    private val twitterClientService: TwitterClientService,
    private val telegramBotExecutorService: TelegramBotExecutorService
) : DefaultTwitterCronJob {

    private val logger = LoggerFactory.getLogger(TwitterCronJob::class.java)

    @Scheduled(fixedDelay = 1000_00)
    fun updateTwitter() {
        updateTwitterLikesForAllUsernames()
        //updateTwittterTweetForAllUsernames()
    }

    //    fun updateTwittterTweetForAllUsernames() {
//        logger.info("Start updater tweets")
//        val usernames = databaseTwitterUserService.getAllUsername()
//        usernames.forEach {
//            updateTwitterTweetForUsername(it)
//            //Thread.sleep(10_000)
//        }
//        logger.info("End updater tweets")
//    }
//
//    fun updateTwitterTweetForUsername(username: String) {
//        val existUsername = databaseTwitterUserService.existUser(username)
//        if (!existUsername) {
//            logger.info("Username = $username not found in db")
//            return
//        }
//
//        val tweetInDB = databaseTwitterUserService.getTwitterUserByUsername(username)
//        val tweetInTwitter = twitterClientService.lastLikeTweetByUsername(username)
//
//        if (tweetInDB == null || tweetInTwitter.id != tweetInDB.lastLikeId) {
//            logger.info("New tweet from $username id = $tweetInTwitter.id")
//            val twitterUser = TwitterUser(
//                username = username,
//                lastLikeId = tweetInTwitter.id,
//                lastTweetId = tweetInDB!!.lastTweetId,
//                chats = tweetInDB.chats
//            )
//            databaseTwitterUserService.updateTwitterUser(twitterUser)
//            logger.info("Update username = $username")
//            val parsedTweet = twitterClientService.parseTweet(tweetInTwitter.id)
//            val chats = databaseTelegramChatService.getChatsByUsername(username)
//            chats.map {
//                logger.info("Send tweet to $it")
//                val typeTweet = TypeByTweet.Like(username, tweetInTwitter.id)
//                telegramBotExecutorService.sendTweet(it, parsedTweet, typeTweet)
//            }
//        }
//    }
//
    fun updateTwitterLikesForAllUsernames() {
        logger.info("Start updater likes")
        val usernames = databaseTwitterUserService.getAllUsername()
        usernames.forEach {
            updateTwitterLikeForUsername(it)
            //Thread.sleep(10_000)
        }
        logger.info("End updater likes")
    }

    fun updateTwitterLikeForUsername(username: String) {
        val existUsername = databaseTwitterUserService.existUser(username)
        if (!existUsername) {
            logger.info("Username = $username not found in db")
            return
        }

        val tweetInDB = databaseTwitterUserService.getTwitterUserByUsername(username)
        val tweetInTwitter = twitterClientService.lastLikeTweetByUsername(username)

        if (tweetInDB == null || tweetInTwitter.id != tweetInDB.lastLikeId) {
            logger.info("New tweet from $username id = $tweetInTwitter.id")
            val twitterUser = TwitterUser(
                username = username,
                lastLikeId = tweetInTwitter.id,
                lastTweetId = tweetInDB!!.lastTweetId,
                chats = tweetInDB.chats
            )
            databaseTwitterUserService.updateTwitterUser(twitterUser)
            logger.info("Update username = $username")
            val parsedTweet = twitterClientService.parseTweet(tweetInTwitter.id)
            val chats = databaseTelegramChatService.getChatsByUsername(username)
            chats.map {
                logger.info("Send tweet to $it")
                val typeTweet = TypeByTweet.Like(username, tweetInTwitter.id)
                telegramBotExecutorService.sendTweet(it, parsedTweet, typeTweet)
            }
        }
    }
}