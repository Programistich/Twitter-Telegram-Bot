package com.programistich.twitter.configuration.twitter

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

@EnableConfigurationProperties(TwitterConfiguration::class)
@Component
class TwitterComponent(
    private val twitterConfiguration: TwitterConfiguration,
) {

    @Bean
    fun tweet(): Twitter {
        val configurationBuilder = ConfigurationBuilder()
        configurationBuilder.apply {
            setDebugEnabled(true)
            setOAuthConsumerKey(twitterConfiguration.api_key)
            setOAuthConsumerSecret(twitterConfiguration.secret_key)
            setOAuthAccessToken(twitterConfiguration.access_token)
            setOAuthAccessTokenSecret(twitterConfiguration.secret_token)
        }
        val twitterFactory = TwitterFactory(configurationBuilder.build())
        return twitterFactory.instance
    }
}