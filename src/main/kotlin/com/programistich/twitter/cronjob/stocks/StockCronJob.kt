package com.programistich.twitter.cronjob.stocks

import org.springframework.stereotype.Service

@Service
interface StockCronJob {
    fun getTeslaStocks()
}