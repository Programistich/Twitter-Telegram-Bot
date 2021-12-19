package com.programistich.twitter.routing.def

import com.programistich.twitter.common.Command

interface DefaultCommandRouter {

    fun parseCommand(typeCommand: Command, chatId:String, textMessage: String)
}