package com.programistich.twitter.cache

import org.springframework.stereotype.Component
import twitter4j.Tweet

@Component
class TweetCache {
    // tweetID - tweet
    private val cache: MutableMap<Long, Tweet> = hashMapOf()

    fun get(tweetId: Long): Tweet? {
        return cache[tweetId]
    }

    fun add(tweet: Tweet): Long {
        cache[tweet.id] = tweet
        return tweet.id
    }
}
