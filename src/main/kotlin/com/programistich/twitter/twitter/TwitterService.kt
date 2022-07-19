package com.programistich.twitter.service.twitter

import com.programistich.twitter.cache.TweetCache
import com.programistich.twitter.telegram.TelegramMessageType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import twitter4j.*

data class BaseTweet(
    val id: Long,
    val url: String,
    val content: TelegramMessageType? = null,
    val author: TwitterAccount,
) {
    fun html(text: String) = "<a href=\"$url\">$text</a>"
}

data class TwitterAccount(
    val id: Long,
    val name: String,
    val username: String,
    val url: String,
) {
    fun html() = "<a href=\"$url\">$name</a>"
}

data class InternalTweet(
    val nextTweet: InternalTweet? = null,
    val current: BaseTweet
)

fun Tweet.newId(): Long? {
    val ids = listOf(
        quotedTweetId,
        retweetId,
        repliedToTweetId
    )
    return ids.filterNotNull().firstOrNull()
}

@Service
class TwitterService(
    private val twitter: Twitter,
    private val cache: TweetCache
) {

    fun parseInternalTweet(tweetId: Long): InternalTweet {
        val tweet = cache.get(tweetId) ?: twitter.getTweets(tweetId).tweets[0]
        cache.add(tweet)
        val nextId = tweet.newId()
        return InternalTweet(
            nextTweet = nextId?.let { parseInternalTweet(it) },
            current = parseTweetForTelegram(tweet)
        )
    }

    fun parseTweetForTelegram(tweet: Tweet): BaseTweet {
        val url = getLinkOnTweet(tweet.id, getUserName(tweet.authorId!!))
        return BaseTweet(
            id = tweet.id,
            url = url,
            content = parseTweet(tweet.id),
            author = parseTwitterAccount(tweet.authorId!!)
        )
    }

    fun parseTweetForTelegram(tweetId: Long): BaseTweet {
        val tweet = cache.get(tweetId) ?: twitter.getTweets(tweetId).tweets[0]
        val author = parseTwitterAccount(tweet.authorId!!)
        val url = getLinkOnTweet(tweetId, author.username)
        return BaseTweet(
            id = tweetId,
            url = url,
            content = parseTweet(tweetId),
            author = author
        )
    }

    fun parseTwitterAccount(accountId: Long): TwitterAccount {
        val author = twitter.showUser(accountId)
        val name = author.name
        val username = author.screenName
        val url = "https://twitter.com/$username"
        return TwitterAccount(
            id = accountId,
            name = name,
            username = username,
            url = url
        )
    }

    fun existUsernameInTwitter(username: String): Boolean {
        return kotlin.runCatching {
            twitter.showUser(username)
        }.isSuccess
    }

    fun lastLikeByUsername(username: String): Tweet {
        val user = twitter.showUser(username)
        return twitter.getLikedTweets(userId = user.id, maxResults = 5).tweets.first()
    }

    fun lastTweetByUsername(username: String): Tweet {
        val status = twitter.getUserTimeline(username)
        val tweetId = status[0].id
        return cache.get(tweetId) ?: twitter.getTweets(tweetId).tweets[0]
    }

    fun parseTweet(tweetId: Long): TelegramMessageType? {
        val tweet = twitter.showStatus(tweetId)
        val urlEntity = tweet.quotedStatusPermalink
        var text = tweet.text
        if (urlEntity != null) {
            text = text.replace(urlEntity.url, "")
        }
        val medias = tweet.mediaEntities
        var telegramMessageType: TelegramMessageType? = null
        if (medias.isEmpty()) telegramMessageType = TelegramMessageType.TextMessage(text)
        else {
            medias.toList().forEach {
                val link = it.url
                text = text.replace(link, "")
            }
            if (medias.size == 1) {
                when (medias[0].type) {
                    "photo" -> telegramMessageType = TelegramMessageType.PhotoMessage(medias[0].mediaURL, text)
                    "video" -> {
                        val urlVideo = medias[0].videoVariants.toList().sortedBy {
                            it.bitrate
                        }.reversed().map { it.url }
                        telegramMessageType = TelegramMessageType.VideoMessage(urlVideo, text)
                    }
                    "animated_gif" -> {
                        if (medias[0].videoVariants.isNotEmpty()) {
                            val urlVideo = medias[0].videoVariants.toList().sortedBy {
                                it.bitrate
                            }.reversed().map { it.url }
                            telegramMessageType = TelegramMessageType.VideoMessage(urlVideo, text)
                        } else telegramMessageType = TelegramMessageType.AnimatedMessage(medias[0].mediaURL, text)
                    }
                }
            } else {
                val url = arrayListOf<String>()
                for (media in medias) {
                    when (media.type) {
                        "photo" -> url.add(media.mediaURL)
//                        "video" -> {
//                            val urlVideo = medias[0].videoVariants.toList().sortedBy {
//                                it.bitrate
//                            }.reversed()[0].url
//                            url.add(urlVideo)
//                        }
                    }
                }
                telegramMessageType = TelegramMessageType.ManyMediaMessage(url, text)
            }
        }
        return telegramMessageType
    }

    fun usernameToLink(text: String): String {
        val result = arrayListOf<String>()
        text.split(" ").toList().map {
            if (it.startsWith("@") && existUsernameInTwitter(it)) {
                val url = urlUser(it)
                result.add("<a href=\"$url\">$it</a>")
            } else result.add(it)
        }
        var resultString = ""
        result.forEach {
            resultString += "$it "
        }
        return resultString
    }

    fun getUser(username: String): User {
        return twitter.showUser(username)
    }

    fun getUserName(id: Long): String {
        return twitter.showUser(id).name
    }

    fun nameUser(username: String): String {
        return getUser(username).name
    }

    fun urlUser(username: String): String {
        return "https://twitter.com/$username"
    }

    fun getTweetById(tweetId: Long): Tweet {
        val tweets = twitter.getTweets(tweetId)
        println("Tweets by id $tweetId")
        tweets.tweets.forEach {
            println(it)
        }
        return cache.get(tweetId) ?: tweets.tweets.first { it.id == tweetId }
    }

    fun getAuthorForTweet(tweet: Tweet): String {
        return twitter.showUser(tweet.authorId!!).screenName
    }

    fun getLinkOnTweet(tweetId: Long, username: String): String {
        return "https://twitter.com/$username/status/$tweetId"
    }

    fun existTweetId(tweetId: Long): Boolean {
        return runCatching {
            getTweetById(tweetId)
        }.isSuccess
    }
}
