package com.programistich.twitter.cronjob

import com.programistich.twitter.service.stocks.StocksService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class StockCronJob(
    private val stockService: StocksService,
)  {

    @Scheduled(cron = "0 0 22 * * *")
    fun getTeslaStocks() {
        stockService.sendStock("-1001488807577", null, stockService.getStock("TSLA"))
    }
}
