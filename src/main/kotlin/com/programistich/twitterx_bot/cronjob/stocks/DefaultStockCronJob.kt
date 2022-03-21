package com.programistich.twitterx_bot.cronjob.stocks

import com.programistich.twitterx_bot.service.db.DatabaseTelegramChatService
import com.programistich.twitterx_bot.service.stocks.StocksService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class DefaultStockCronJob(
    private val stockService: StocksService,
    private val databaseTelegramChatService: DatabaseTelegramChatService
) : StockCronJob {

    @Scheduled(cron = "0 0 22 * * *")
    override fun getTeslaStocks() {
        databaseTelegramChatService.getAllChats().forEach {
            stockService.sendStock(it.chatId, null, stockService.getStock("TSLA"))
        }
    }
}
