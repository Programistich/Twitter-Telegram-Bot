package com.programistich.twitter.routing.def

import org.telegram.telegrambots.meta.api.objects.Update

interface DefaultRouter {
    fun parseMessage(update: Update)
}