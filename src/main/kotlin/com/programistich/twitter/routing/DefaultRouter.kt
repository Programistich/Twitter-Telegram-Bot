package com.programistich.twitter.routing

import com.programistich.twitter.common.Command
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

interface DefaultRouter {
    fun parseMessage(update: Update)
    fun parseMessageWithCommand(commandEnum: Command, update: Update)
    fun parseTextMessage(message: Message)
}