package com.programistich.twitter.common

sealed class TypeByTweet {
    data class Like(val username: String, val tweetId: Long, val last: Boolean = false) : TypeByTweet()
    data class Get(val username: String, val link: String, val author: String) : TypeByTweet()
}