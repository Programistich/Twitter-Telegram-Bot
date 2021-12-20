package com.programistich.twitter.service.twitter

import com.programistich.twitter.common.TypeMessage
import org.springframework.stereotype.Service
import twitter4j.*

@Service
class TwitterClientService(
    private val twitter: Twitter
) : DefaultTwitterClientService {

    override fun existUsernameInTwitter(username: String): Boolean {
        return try {
            val user = twitter.showUser(username)
            true
        } catch (e: TwitterException) {
            false
        }
    }


    override fun lastLikeTweetByUsername(username: String): Tweet {
        val user = twitter.showUser(username)
        return twitter.getLikedTweets(user.id).tweets[0]
    }

    override fun parseTweet(tweetId: Long): TypeMessage? {
        val tweet = twitter.showStatus(tweetId)
        val urlEntity = tweet.quotedStatusPermalink
        var text = tweet.text
        if (urlEntity != null) {
            text = text.replace(urlEntity.url, "")
        }
        val medias = tweet.mediaEntities
        var typeMessage: TypeMessage? = null
        if (medias.isEmpty()) typeMessage = TypeMessage.TextMessage(text)
        else {
            if (medias.size == 1) {
                when (medias[0].type) {
                    "photo" -> typeMessage = TypeMessage.PhotoMessage(medias[0].mediaURL, text)
                    "video" -> {
                        val urlVideo = medias[0].videoVariants.toList().sortedBy {
                            it.bitrate
                        }.reversed()[0].url
                        typeMessage = TypeMessage.VideoMessage(urlVideo, text)
                    }
                    "animated_gif" -> typeMessage = TypeMessage.AnimatedMessage(medias[0].mediaURL, text)
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
                typeMessage = TypeMessage.ManyMediaMessage(url, text)
            }
        }
        return typeMessage
    }

    override fun getUser(username: String): User {
        return twitter.showUser(username)
    }

    override fun nameUser(username: String): String {
        return getUser(username).name
    }

    override fun urlUser(username: String): String {
        return "https://twitter.com/$username"
    }

    override fun getTweetById(tweetId: Long): Tweet {
        return twitter.getTweets(tweetId).tweets[0]
    }

    fun getAuthorForTweet(tweet: Tweet): String {
        return twitter.showUser(tweet.authorId!!).screenName
    }

    fun getLinkOnTweet(tweetId: Long, username: String): String {
        return "https://twitter.com/$username/status/$tweetId"
    }

}