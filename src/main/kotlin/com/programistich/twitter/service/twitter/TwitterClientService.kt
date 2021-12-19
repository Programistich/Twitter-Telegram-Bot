package com.programistich.twitter.service.twitter

import com.programistich.twitter.common.TypeMessage
import org.springframework.stereotype.Service
import twitter4j.Tweet
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.getLikedTweets

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
        val text = tweet.text
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

}