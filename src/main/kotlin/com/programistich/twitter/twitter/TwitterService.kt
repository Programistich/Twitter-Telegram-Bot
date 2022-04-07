package com.programistich.twitter.service.twitter

import com.programistich.twitter.common.TypeMessageTelegram
import org.springframework.stereotype.Service
import twitter4j.*

data class BaseTweet(
    val id: Long,
    val url: String,
    val content: TypeMessageTelegram? = null,
    val author: TwitterAccount
){
    fun html(text: String) = "<a href=\"$url\">$text</a>"
}

data class TwitterAccount(
    val id: Long,
    val name: String,
    val username: String,
    val url: String
){
    fun html() = "<a href=\"$url\">$name</a>"
}

@Service
class TwitterService(
    private val twitter: Twitter,
) {


    fun parseTweetForTelegram(tweetId: Long): BaseTweet {
        val tweet = twitter.getTweets(tweetId).tweets[0]
        val author = parseTwitterAccount(tweet.authorId!!)
        val url = getLinkOnTweet(tweetId, author.username)
        return BaseTweet(
            id = tweetId,
            url = url,
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
        return twitter.getLikedTweets(userId = user.id, maxResults = 5).tweets[0]
    }

    fun lastTweetByUsername(username: String): Tweet {
        try {
            val status = twitter.getUserTimeline(username)[0]
            val tweet = twitter.getTweets(status.id)
            return tweet.tweets[0]
        }
        catch (exception: TwitterException){
            throw TwitterException("Status null")
        }
    }


    fun parseTweet(tweetId: Long): TypeMessageTelegram? {
        val tweet = twitter.showStatus(tweetId)
        val urlEntity = tweet.quotedStatusPermalink
        var text = tweet.text
        if (urlEntity != null) {
            text = text.replace(urlEntity.url, "")
        }
        val medias = tweet.mediaEntities
        var typeMessageTelegram: TypeMessageTelegram? = null
        if (medias.isEmpty()) typeMessageTelegram = TypeMessageTelegram.TextMessage(text)
        else {
            medias.toList().forEach {
                val link = it.url
                text = text.replace(link, "")
            }
            if (medias.size == 1) {
                when (medias[0].type) {
                    "photo" -> typeMessageTelegram = TypeMessageTelegram.PhotoMessage(medias[0].mediaURL, text)
                    "video" -> {
                        val urlVideo = medias[0].videoVariants.toList().sortedBy {
                            it.bitrate
                        }.reversed()[0].url
                        typeMessageTelegram = TypeMessageTelegram.VideoMessage(urlVideo, text)
                    }
                    "animated_gif" -> {
                        if (medias[0].videoVariants.isNotEmpty()) {
                            val urlVideo = medias[0].videoVariants.toList().sortedBy {
                                it.bitrate
                            }.reversed()[0].url
                            typeMessageTelegram = TypeMessageTelegram.VideoMessage(urlVideo, text)
                        } else typeMessageTelegram = TypeMessageTelegram.AnimatedMessage(medias[0].mediaURL, text)
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
                typeMessageTelegram = TypeMessageTelegram.ManyMediaMessage(url, text)
            }
        }
        return typeMessageTelegram
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


    fun nameUser(username: String): String {
        return getUser(username).name
    }


    fun urlUser(username: String): String {
        return "https://twitter.com/$username"
    }


    fun getTweetById(tweetId: Long): Tweet {
        return twitter.getTweets(tweetId).tweets[0]
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
