package com.programistich.twitter.common

sealed class TypeCommand(val tweetId: Long) {
    class Like(val username: String, tweetId: Long, val last: Boolean = false) : TypeCommand(tweetId)
    class Get(tweetId: Long, val author: String) : TypeCommand(tweetId)
    class Tweet(tweetId: Long, val last: Boolean = false) : TypeCommand(tweetId)
}
