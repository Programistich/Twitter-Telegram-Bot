package com.programistich.twitter.repository

import com.programistich.twitter.model.TwitterUser
import org.springframework.data.jpa.repository.JpaRepository

interface TwitterUserRepository : JpaRepository<TwitterUser, String>