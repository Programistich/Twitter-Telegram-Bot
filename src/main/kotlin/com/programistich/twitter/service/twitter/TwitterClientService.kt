package com.programistich.twitter.service.twitter

import com.programistich.twitter.common.TypeMessageTelegram
import twitter4j.Status
import twitter4j.Tweet
import twitter4j.User

interface TwitterClientService {

    fun existUsernameInTwitter(username: String): Boolean
    fun lastLikeByUsername(username: String): Tweet
    fun lastTweetByUsername(username: String): Tweet
    fun parseTweet(tweetId: Long): TypeMessageTelegram?
    fun usernameToLink(text: String): String
    fun getUser(username: String): User
    fun nameUser(username: String): String
    fun urlUser(username: String): String
    fun getTweetById(tweetId: Long): Tweet
    fun getAuthorForTweet(tweet: Tweet): String
    fun getLinkOnTweet(tweetId: Long, username: String): String
    fun getUserNameByTweetId(tweetId: Long): String
}
