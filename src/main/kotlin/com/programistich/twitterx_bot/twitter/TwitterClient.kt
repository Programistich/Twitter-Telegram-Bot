package com.programistich.twitterx_bot.twitter

import org.springframework.stereotype.Service
import twitter4j.Twitter

@Service
class TwitterClient(
    val twitter: Twitter
) {
    fun parseTweet(tweetId: Long): Tweet{
        return Tweet(tweetId)
    }
}
