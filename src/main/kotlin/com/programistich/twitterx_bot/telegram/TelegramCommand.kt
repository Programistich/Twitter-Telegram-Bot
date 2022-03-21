package com.programistich.twitterx_bot.telegram

enum class TelegramCommand(val command: String) {
    START("/start"),
    NEW_TWITTER_USER("/new"),
    NEW("/new"),
    LAST_LIKE("/last_like"),
    LAST_TWEET("/last_tweet"),
    GET("/get"),
    PICTURE("/pic"),
    DONATE("/donate"),
    HELP("/help"),
    STOCKS("/stock"),
    PING("/ping");

    companion object {
        val LOOKUP = values().associateBy(TelegramCommand::command)
    }
}
