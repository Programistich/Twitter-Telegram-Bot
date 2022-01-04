package com.programistich.twitter

import com.programistich.twitter.service.twitter.TwitterClientService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import twitter4j.Twitter

@SpringBootApplication
@EnableScheduling
class TwitterApplication

fun main() {
    runApplication<TwitterApplication>()
}

