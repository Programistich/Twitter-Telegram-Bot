package com.programistich.twitter.common

enum class Command(val command: String) {
    START("/start"),
    NEW("/new"),
    LAST_LIKE("/last_like"),
    LAST_TWEET("/last_tweet"),
    GET("/get"),
    PICTURE("/pic"),
    DONATE("/donate"),
    PING("/ping");

    companion object {
        val LOOKUP = values().associateBy(Command::command)
    }
}