package com.programistich.twitter.service.db

import com.programistich.twitter.entity.TwitterUser

interface DatabaseTwitterUserService {
    fun existUser(username: String): Boolean
    fun getTwitterUserByUsername(username: String): TwitterUser?
    fun lastLikeByUsername(username: String): Long?
    fun lastTweetByUsername(username: String): Long?
    fun getAllUsername(): List<String>
    fun updateTwitterUser(twitterUser: TwitterUser)
}
