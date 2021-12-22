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
class TranslateService : DefaultTranslateService {

    private val URL = "https://translate.yandex.net/api/v1.5/tr.json"

    @Value("\${yandex.translate.api}")
    private lateinit var key: String

    override fun translate(text: String): String {
        if (text.isEmpty()) return ""

        var lang = detectLanguage(text)
        if (lang == null) lang = "en"
        val pairTranslated = translateText(setupText(text), lang) ?: return text

        var translatedText: String = pairTranslated.first
        println(translatedText)
        translatedText = replaceSome(translatedText)
        translatedText = deleteUnicode(translatedText)

        val langs = pairTranslated.second.split("-")
        val firstLang = langs[0].uppercase()
        val secondLang = langs[1].uppercase()

        if (firstLang == secondLang) return translatedText
        return "[$firstLang]\n$text\n\n[$secondLang]\n$translatedText"
    }

    private fun translateText(text: String, lang: String): Pair<String, String>? {
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

    private fun detectLanguage(text: String): String? {
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

    private fun replaceSome(text: String): String {
        return text
            .replace("\\n", "\n")
            .replace("[\"", "")
            .replace("\"]", "")
            .replace("\\\"", "")
    }

    private fun setupText(text: String): String {
        return text
            .replace("“", "\\“")
            .replace("”", "\\”")
            .replace("—", "\\—")
    }

    private fun deleteUnicode(text: String): String {
        return text
    }
}