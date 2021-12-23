package com.programistich.twitter.service.twitter

import com.programistich.twitter.common.TypeMessageTelegram
import twitter4j.Tweet
import twitter4j.User

interface DefaultTwitterClientService {

    fun existUsernameInTwitter(username: String): Boolean
    fun lastLikeTweetByUsername(username: String): Tweet
    fun parseTweet(tweetId: Long): TypeMessageTelegram?
    fun getUser(username: String): User
    fun nameUser(username: String): String
    fun urlUser(username: String): String
    fun getTweetById(tweetId: Long): Tweet
}