package com.programistich.twitter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TwitterApplication

fun main() {
    System.getProperties()["proxySet"] = "true"
    System.getProperties()["socksProxyHost"] = "127.0.0.1"
    System.getProperties()["socksProxyPort"] = "9150"
    runApplication<TwitterApplication>()
}

