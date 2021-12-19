package com.programistich.twitter.common

sealed class TypeMessage {
    data class TextMessage(val text: String) : TypeMessage()
    data class PhotoMessage(val url: String, val caption: String) : TypeMessage()
    data class VideoMessage(val url: String, val caption: String) : TypeMessage()
    data class AnimatedMessage(val url: String, val caption: String) : TypeMessage()
    data class ManyMediaMessage(val urlsMedia: List<String>, val caption: String) : TypeMessage()
}
