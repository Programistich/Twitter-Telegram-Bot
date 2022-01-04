package com.programistich.twitter.service.stocks

import com.programistich.twitter.service.telegram.TelegramExecutorService
import org.springframework.stereotype.Service
import yahoofinance.Stock
import yahoofinance.YahooFinance
import java.math.BigDecimal
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
        val symbol = stock.stats.symbol
        val price = quote.price.toInt()
        val open = quote.open
        val close = quote.previousClose
        val percent = ((open - close) / close).multiply(BigDecimal(100))
        val dayLow = quote.dayLow.toInt()
        val dayHigh = quote.dayHigh.toInt()
        val volume = (quote.volume / 1_000_000).toInt()
        val avgVolume = (quote.avgVolume / 1_000_000).toInt()
        return """
            Акции: <b>${stock.name} ($symbol)</b>
            Дата: <b>$date</b>
            Цена: <b>$price$ ($percent%)</b>
            Цена открытия: <b>${open.toInt()}$</b>
            Цена закрытия: <b>${close.toInt()}$</b>
            Диапозон цен: <b>$dayLow$ - $dayHigh$</b>
            Объём торгов за сегодня: <b>${volume}M</b>
            Средний Объём торгов за год: <b>${avgVolume}M</b>
        """.trimIndent()
    }

    override fun sendStock(chatId: String, messageId: Int?, stock: Stock) {
        val text = aboutStock(stock)
        telegramExecutorService.sendTextMessage(chatId, text, messageId)
    }

    override fun sendStock(chatId: String, messageId: Int?, name: String) {
        val stock = YahooFinance.get(name)
        sendStock(chatId, messageId, stock)
    }
}