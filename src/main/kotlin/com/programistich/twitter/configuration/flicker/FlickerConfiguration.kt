package com.programistich.twitter.configuration.flicker

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "flicker")
data class FlickerConfiguration(
    var key: String = "",
    var secret: String = "",
    var token: String = "",
    var tokensecret: String = "",
)