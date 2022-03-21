package com.programistich.twitterx_bot.repository

import com.programistich.twitterx_bot.entity.TwitterAccount
import org.springframework.data.jpa.repository.JpaRepository

interface TwitterUserRepository : JpaRepository<TwitterAccount, String>
