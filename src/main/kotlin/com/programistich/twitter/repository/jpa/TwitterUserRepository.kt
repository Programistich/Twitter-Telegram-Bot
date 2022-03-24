package com.programistich.twitter.repository.jpa

import com.programistich.twitter.entity.TwitterUser
import org.springframework.data.jpa.repository.JpaRepository

interface TwitterUserRepository : JpaRepository<TwitterUser, String>
