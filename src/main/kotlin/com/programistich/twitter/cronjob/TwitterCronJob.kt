package com.programistich.twitter.cronjob

import com.programistich.twitter.cache.TweetCache
import com.programistich.twitter.entity.TwitterUser
import com.programistich.twitter.service.db.DefaultDatabaseTelegramChatService
import com.programistich.twitter.service.db.DefaultDatabaseTwitterUserService
import com.programistich.twitter.service.telegram.DefaultTelegramExecutorService
import com.programistich.twitter.service.twitter.TwitterService
import com.programistich.twitter.template.Template
import com.programistich.twitter.template.TemplateReader
import com.programistich.twitter.utils.TypeCommand
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import twitter4j.Tweet
import java.util.concurrent.TimeUnit

@Service
class TwitterCronJob(
    private val defaultDatabaseTwitterUserService: DefaultDatabaseTwitterUserService,
    private val twitterService: TwitterService,
    private val telegramExecutorService: DefaultTelegramExecutorService,
    private val cache: TweetCache,
    private val template: TemplateReader,
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
        val tweetInTwittersDirty = twitterService.lastTweetByUsername(username)
        tweetInTwittersDirty.forEach { tweetInTwitter ->
            if (cache.get(tweetInTwitter.id) == null) {
                logger.info("New tweet from $username id = ${tweetInTwitter.id}")
                user.lastTweetId = tweetInTwitter.id
                defaultDatabaseTwitterUserService.updateTwitterUser(user)
                logger.info("Update username = $username")
                kotlin.runCatching {
                    val internalTweet = twitterService.parseInternalTweet(tweetInTwitter.id) ?: return
                    cache.add(tweetInTwitter)
                    user.chats.map { chat ->
                        if (chat.isChannel && tweetInTwitter.retweetId != null) return
                        else telegramExecutorService.sendTweetEntryPoint(internalTweet, chat.chatId)
                    }
                }.onFailure {
                    logger.info("Update username = $username error")
                }
            }
        }
    }

    @Transactional
    fun updateLikeForUsername(username: String, user: TwitterUser) {
        logger.info("Update like twitter account $username")
        val tweetInTwittersDirty = twitterService.lastLikeByUsername(username)
        tweetInTwittersDirty.forEach { tweetInTwitter ->
            if (cache.getLike(tweetInTwitter.id) == null) {
                logger.info("New like from $username id = $tweetInTwitter.id")
                user.lastLikeId = tweetInTwitter.id
                defaultDatabaseTwitterUserService.updateTwitterUser(user)
                logger.info("Update username = $username")
                kotlin.runCatching {
                    val parsedTweet = twitterService.parseTweet(tweetInTwitter.id)
                    cache.addLike(tweetInTwitter)
                    user.chats.filter { !it.isChannel }.map {
                        logger.info("Send like tweet to ${it.chatId}")
                        val typeTweet = TypeCommand.Like(username, tweetInTwitter.id)
                        telegramExecutorService.sendTweet(it.chatId, parsedTweet, typeTweet)
                    }
                }
            }
        }
    }
}
