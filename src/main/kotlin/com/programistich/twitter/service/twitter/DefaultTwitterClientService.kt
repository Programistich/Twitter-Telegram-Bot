package com.programistich.twitter.service.twitter

import com.programistich.twitter.common.TypeMessage
import twitter4j.Tweet

interface DefaultTwitterClientService {

    fun existUsernameInTwitter(username: String): Boolean

    fun lastLikeTweetByUsername(username: String): Tweet

    fun parseTweet(tweetId: Long): TypeMessage?
}