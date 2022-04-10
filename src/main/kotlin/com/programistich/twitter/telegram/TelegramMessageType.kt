package com.programistich.twitter.telegram

sealed class TelegramMessageType {
    data class TextMessage(val text: String) : TelegramMessageType()
    data class PhotoMessage(val url: String, val text: String) : TelegramMessageType()
    data class VideoMessage(val url: List<String>, val text: String) : TelegramMessageType()
    data class AnimatedMessage(val url: String, val text: String) : TelegramMessageType()
    data class ManyMediaMessage(val urlsMedia: List<String>, val text: String) : TelegramMessageType()
}
