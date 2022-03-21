package com.programistich.twitterx_bot.repository

import com.programistich.twitterx_bot.entity.TelegramChat
import org.springframework.data.jpa.repository.JpaRepository

interface TelegramChatRepository : JpaRepository<TelegramChat, String>
