package com.programistich.twitter.telegram

enum class TelegramBotCommand(val value: String) {
    START("/start"),
    ADD("/add"),
    GET("/get"),
    DONATE("/donate"),
    HELP("/help"),
    STOCKS("/stock"),
    PING("/ping");

    companion object {
        val LOOKUP = values().associateBy(TelegramBotCommand::value)
    }
}
