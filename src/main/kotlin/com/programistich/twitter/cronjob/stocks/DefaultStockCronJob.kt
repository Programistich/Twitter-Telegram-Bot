package com.programistich.twitter.cronjob.stocks

import com.programistich.twitter.service.db.DatabaseTelegramChatService
import com.programistich.twitter.service.stocks.StocksService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class DefaultStockCronJob(
    private val stockService: StocksService,
) : StockCronJob {

    @Scheduled(cron = "0 0 22 * * *")
    override fun getTeslaStocks() {
        stockService.sendStock("-1001488807577", null, stockService.getStock("TSLA"))
    }
}
