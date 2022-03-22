package com.programistich.twitter.service.translate

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException


@Service
class DefaultTranslateService : TranslateService {

    private val URL = "https://translate.yandex.net/api/v1.5/tr.json"

    @Value("\${yandex.translate.api}")
    private lateinit var key: String

    override fun translateText(text: String): String {
        if (text.isEmpty()) return ""
        var lang = requestDetectLanguage(text)
        if (lang == null) lang = "en"

        val beforeText = formatTextBeforeTranslate(text)
        val translatedPair = requestTranslate(beforeText, lang) ?: return text

        val translatedText: String = translatedPair.first
        val afterText = formatTextAfterTranslate(translatedText)

        val langs = translatedPair.second.split("-")
        val firstLang = langs[0].uppercase()
        val secondLang = langs[1].uppercase()

        if (firstLang == secondLang) return text
        return "[$firstLang]: ${text.replace("<", "")}\n\n[$secondLang]: $afterText"
    }

    override fun requestTranslate(text: String, lang: String): Pair<String, String>? {
        val okHttpClient = OkHttpClient()
        val urlFinal = "$URL/translate?lang=$lang-ru&format=html&key=$key"
        val body: RequestBody = FormBody.Builder().add("text", text).build()
        val request: Request = Request.Builder()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .url(urlFinal)
            .post(body)
            .build()
        return try {
            val response = okHttpClient.newCall(request).execute()
            val responseData = response.body!!.string()
            val jsonObject = JSONObject(responseData)
            val resultText = jsonObject["text"].toString()
            val langs = jsonObject["lang"].toString()
            Pair(resultText, langs)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun requestDetectLanguage(text: String): String? {
        val okHttpClient = OkHttpClient()
        val urlFinal = "$URL/detect?hint=en,ru&key=$key"
        val body: RequestBody = FormBody.Builder().add("text", text).build()
        val request: Request = Request.Builder()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .url(urlFinal)
            .post(body)
            .build()
        try {
            val response = okHttpClient.newCall(request).execute()
            val responseData = response.body!!.string()
            val jsonObject = JSONObject(responseData)
            val lang = jsonObject["lang"].toString()
            if (lang.isEmpty()) return null
            return lang
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun formatTextBeforeTranslate(text: String): String {
        var result: String = text
        val firstChar = text[0]
        if (firstChar == '.') result = text.drop(1)
        return result
            .replace("Irony Man", "Железный человек")
            .replace("Elon's", "Илона")
            .replace("BREAKING", "Последняя новость")
            .replace("AP", "AutoPilot")
            .replace("supercharging", "Tesla SuperCharger")
    }


    private fun formatTextAfterTranslate(text: String): String {
        return text
            .replace("@элон Маск", "@elonmusk")
            .replace("@фредерик ламберт", "@fredericlambert")
            .replace("@ элон Маск", "@elonmusk")
            .replace("@Джорджлукасильм", "@GeorgeLucasILM")
            .replace("@тесла_адри", "@tesla_adri")
            .replace("@нуродев", "@nurodev")
            .replace("@Элон Маск", "@elonmusk")
            .replace("@Тесла", "@Tesla")
            .replace("@Настоящий футурист", "@Real_Futurist")
            .replace("\$ТСЛА", "\$TSLA")
            .replace("\$ТСЛА", "\$TSLA")
            .replace("#PlaidМоделИ", "#PlaidМоделS")
            .replace("@элонмуск", "@elonmusk")
            //##Tesla #Платные модели идентифицируют, выбирают и паркуются в стойлах с наддувом

            .replace("\\n", "\n")
            .replace("[\"", "")
            .replace("\"]", "")
            .replace("\\\"", "")

            .replace("\\u201c", "“")
            .replace("\\u201d", "”")
            .replace("\\u2014", "—")
            .replace("\\u2013", "–")
            .replace("\\u2026", "...")
            .replace("\\u2022", "•")
    }

}
