package com.programistich.twitter.common

sealed class TypeCommand {
    data class Like(val username: String, val tweetId: Long, val last: Boolean = false) : TypeCommand()
    data class Get(val username: String, val link: String, val author: String) : TypeCommand()
}