package com.programistich.twitter.common

sealed class TypeMessageTelegram {
    data class TextMessage(val text: String) : TypeMessageTelegram()
    data class PhotoMessage(val url: String, val text: String) : TypeMessageTelegram()
    data class VideoMessage(val url: String, val text: String) : TypeMessageTelegram()
    data class AnimatedMessage(val url: String, val text: String) : TypeMessageTelegram()
    data class ManyMediaMessage(val urlsMedia: List<String>, val text: String) : TypeMessageTelegram()
}
