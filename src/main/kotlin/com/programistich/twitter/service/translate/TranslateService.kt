package com.programistich.twitter.service.translate

import org.springframework.stereotype.Service

@Service
class TranslateService : DefaultTranslateService {

    override fun translate(text: String): String {
        return text
    }
}