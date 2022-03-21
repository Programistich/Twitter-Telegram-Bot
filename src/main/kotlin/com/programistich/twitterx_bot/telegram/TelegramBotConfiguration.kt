package com.programistich.twitterx_bot.telegram

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "telegram")
data class TelegramBotConfiguration(
    var token: String = "",
    var username: String = "",
)
