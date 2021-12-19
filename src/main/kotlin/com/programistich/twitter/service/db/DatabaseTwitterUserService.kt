package com.programistich.twitter.service.db

import com.programistich.twitter.model.TwitterUser
import com.programistich.twitter.repository.TwitterUserRepository
import org.springframework.stereotype.Service

@Service
class DatabaseTwitterUserService(
    private val twitterUserRepository: TwitterUserRepository
) : DefaultDatabaseTwitterUserService {

    override fun existUser(username: String): Boolean {
        val user = twitterUserRepository.findById(username).orElse(null)
        return user != null
    }

    override fun getTwitterUserByUsername(username: String): TwitterUser? {
        return twitterUserRepository.findById(username).orElse(null)
    }

    override fun lastLikeByUsername(username: String): Long? {
        return twitterUserRepository.getById(username).lastLikeId
    }

    override fun getAllUsername(): List<String> {
        return twitterUserRepository.findAll().map { it.username }
    }

    override fun updateTwitterUser(twitterUser: TwitterUser) {
        twitterUserRepository.save(twitterUser)
    }


}