package com.programistich.twitter.configuration.twitter

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "twitter")
data class TwitterConfiguration(
    var api_key: String = "",
    var secret_key: String = "",
    var access_token: String = "",
    var secret_token: String = "",
    var bearer_token: String = "",
)