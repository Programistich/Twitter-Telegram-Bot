package com.programistich.twitter.configuration.twitter

import io.github.redouane59.twitter.TwitterClient
import io.github.redouane59.twitter.signature.TwitterCredentials
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
    fun twitterClient(): TwitterClient {
        return TwitterClient(
            TwitterCredentials.builder()
                .accessToken(twitterConfiguration.access_token)
                .accessTokenSecret(twitterConfiguration.secret_token)
                .apiKey(twitterConfiguration.api_key)
                .apiSecretKey(twitterConfiguration.secret_key)
                .bearerToken(twitterConfiguration.bearer_token)
                .build()
        )
    }

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