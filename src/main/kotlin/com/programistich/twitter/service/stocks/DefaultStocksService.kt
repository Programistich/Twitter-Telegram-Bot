package com.programistich.twitter.service.stocks

import com.programistich.twitter.service.telegram.TelegramExecutorService
import org.springframework.stereotype.Service
import yahoofinance.Stock
import yahoofinance.YahooFinance
import java.text.SimpleDateFormat
import java.util.*

@Service
class DefaultStocksService(
    private val telegramExecutorService: TelegramExecutorService
) : StocksService {

    override fun getStock(name: String): Stock {
        return YahooFinance.get(name)
    }

    override fun aboutStock(stock: Stock): String {
        val quote = stock.quote
        val date = SimpleDateFormat("dd-MM-yyyy").format(Date())
        val price = quote.price.toInt()
        val open = quote.open.toInt()
        val previousClose = quote.previousClose.toInt()
        val dayLow = quote.dayLow.toInt()
        val dayHigh = quote.dayHigh.toInt()
        val volume = (quote.volume / 100_000).toInt()
        val avgVolume = (quote.avgVolume / 100_00).toInt()
        return """
            Акции: <b>${stock.name}</b>
            Дата: <b>$date</b>
            Цена: <b>$price$</b>
            Цена открытия: <b>$open$</b>
            Цена закрытия: <b>$previousClose$</b>
            Диапозон цен: <b>$dayLow$ - $dayHigh$</b>
            Объём торгов за сегодня: <b>${volume}M</b>
            Объём торгов за год: <b>${avgVolume}M</b>
        """.trimIndent()
    }

    override fun sendStock(chatId: String, stock: Stock) {
        val text = aboutStock(stock)
        telegramExecutorService.sendTextMessage(chatId, text)
    }
}