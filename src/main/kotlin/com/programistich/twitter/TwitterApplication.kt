package com.programistich.twitter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TwitterApplication

fun main() {
    runApplication<TwitterApplication>()
}

