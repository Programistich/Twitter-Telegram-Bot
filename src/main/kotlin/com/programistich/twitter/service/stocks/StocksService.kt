package com.programistich.twitter.service.stocks

import yahoofinance.Stock

interface StocksService {
    fun getStock(name: String): Stock
    fun aboutStock(stock: Stock): String
    fun sendStock(chatId: String, stock: Stock)
}