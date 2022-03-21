package com.programistich.twitterx_bot.service.twitter

import com.programistich.twitterx_bot.common.TypeMessageTelegram
import twitter4j.Tweet
import twitter4j.User

interface TwitterClientService {

    fun existUsernameInTwitter(username: String): Boolean
    fun lastLikeTweetByUsername(username: String): Tweet
    fun parseTweet(tweetId: Long): TypeMessageTelegram?
    fun usernameToLink(text: String): String
    fun getUser(username: String): User
    fun nameUser(username: String): String
    fun urlUser(username: String): String
    fun getTweetById(tweetId: Long): Tweet
    fun getAuthorForTweet(tweet: Tweet): String
    fun getLinkOnTweet(tweetId: Long, username: String): String
}
