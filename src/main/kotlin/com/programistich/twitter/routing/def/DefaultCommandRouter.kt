package com.programistich.twitter.routing.def

import com.programistich.twitter.common.Command
import org.telegram.telegrambots.meta.api.objects.Update

interface DefaultCommandRouter {

    fun parseCommand(typeCommand: Command, update: Update, textMessage: String)
}