package com.programistich.twitter.service.translate

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import org.springframework.stereotype.Service
import twitter4j.JSONObject
import java.io.IOException


@Service
class TranslateService : DefaultTranslateService {

    private val URL = "https://libretranslate.de"
    var JSON: MediaType = MediaType.parse("application/json; charset=utf-8")

    override fun translate(text: String): String {
        if (text.isEmpty()) return text
        val translatedText = translateText(text)
        return if (translatedText == null || translatedText == text) "\uD83C\uDDF7\uD83C\uDDFA $text"
        else "\uD83C\uDDEC\uD83C\uDDE7 $text\n\n\uD83C\uDDF7\uD83C\uDDFA $translatedText"
    }

    private fun translateText(text: String): String? {
        val okHttpClient = OkHttpClient()
        val jsonObject = JSONObject()
        jsonObject.put("q", text)
        jsonObject.put("source", "en")
        jsonObject.put("target", "ru")
        jsonObject.put("format", "html")

        val body = RequestBody.create(JSON, jsonObject.toString())
        val request: Request = Request.Builder()
            .url("$URL/translate")
            .post(body)
            .build()
        try {
            val response = okHttpClient.newCall(request).execute()
            val responseObj = JSONObject(response.body().string())
            println(responseObj)
            val result = responseObj["translatedText"].toString()
            println(result)
            result.ifEmpty { return null }
            return result
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}