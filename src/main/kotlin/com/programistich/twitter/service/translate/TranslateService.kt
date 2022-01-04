package com.programistich.twitter.service.translate

interface TranslateService {

    fun translateText(text: String): String
    fun requestTranslate(text: String, lang: String): Pair<String, String>?
    fun requestDetectLanguage(text: String): String?

}