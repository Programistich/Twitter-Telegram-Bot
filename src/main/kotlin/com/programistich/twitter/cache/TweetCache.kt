package com.programistich.twitter.cache

import org.springframework.stereotype.Component
import twitter4j.Tweet

@Component
class TweetCache {
    // tweetID - tweet
    private val cache: MutableMap<Long, Tweet> = hashMapOf()
    private val likes: MutableMap<Long, Tweet> = hashMapOf()

    fun get(tweetId: Long): Tweet? {
        return cache[tweetId]
    }

    fun add(tweet: Tweet): Long {
        cache[tweet.id] = tweet
        return tweet.id
    }

    fun getLike(tweetId: Long): Tweet? {
        return likes[tweetId]
    }

    fun addLike(tweet: Tweet): Long {
        likes[tweet.id] = tweet
        return tweet.id
    }
}
