package com.programistich.twitterx_bot.cronjob.stocks

import org.springframework.stereotype.Service

@Service
interface StockCronJob {
    fun getTeslaStocks()
}
