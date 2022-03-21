package com.programistich.twitterx_bot.service.db

import com.programistich.twitterx_bot.entity.TwitterAccount

interface DatabaseTwitterUserService {
    fun existUser(username: String): Boolean
    fun getTwitterUserByUsername(username: String): TwitterAccount?
    fun lastLikeByUsername(username: String): Long?
    fun getAllUsername(): List<String>
    fun updateTwitterUser(twitterAccount: TwitterAccount)
}
