package com.programistich.twitterx_bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TwitterApplication

fun main() {
    runApplication<TwitterApplication>()
}

class InternalException(override val message: String) : RuntimeException(message)
