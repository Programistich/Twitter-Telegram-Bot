package com.programistich.twitter.repository

import com.programistich.twitter.model.TelegramChat
import org.springframework.data.jpa.repository.JpaRepository

interface TelegramChatRepository : JpaRepository<TelegramChat, String>