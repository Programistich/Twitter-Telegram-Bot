package com.programistich.twitterx_bot.telegram

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Component
class TelegramBotComponent {

    @Bean
    fun botConfig() = DefaultBotOptions()

    @Bean
    fun telegramBot(telegramBot: TelegramBotInstance): TelegramBotsApi {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(telegramBot)
        return telegramBotsApi
    }
}
