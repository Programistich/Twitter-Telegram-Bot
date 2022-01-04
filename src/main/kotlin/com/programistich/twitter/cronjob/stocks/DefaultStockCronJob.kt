package com.programistich.twitter.cronjob.stocks

import com.programistich.twitter.service.db.DatabaseTelegramChatService
import com.programistich.twitter.service.stocks.StocksService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class DefaultStockCronJob(
    private val stockService: StocksService,
    private val databaseTelegramChatService: DatabaseTelegramChatService
) : StockCronJob {

    @Scheduled(cron = "0 0 10 * * *")
    //@Scheduled(fixedDelay = 1000)
    override fun getTeslaStocks() {
        databaseTelegramChatService.getAllChats().forEach {
            stockService.sendStock(it.chatId, null, stockService.getStock("TSLA"))
        }
    }
}