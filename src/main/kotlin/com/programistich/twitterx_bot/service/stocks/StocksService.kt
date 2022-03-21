package com.programistich.twitterx_bot.service.stocks

import yahoofinance.Stock

interface StocksService {
    fun getStock(name: String): Stock
    fun aboutStock(stock: Stock): String
    fun sendStock(chatId: String, messageId: Int?, stock: Stock)
    fun sendStock(chatId: String, messageId: Int?, stock: String)
}
