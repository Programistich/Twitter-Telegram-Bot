package com.programistich.twitterx_bot.repository

import com.programistich.twitterx_bot.entity.TelegramChat
import com.programistich.twitterx_bot.entity.TwitterAccount
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class Repository(
    private val twitterUserRepository: TwitterUserRepository,
    private val telegramChatRepository: TelegramChatRepository,
) {

    fun addTwitterAccount(
        username: String,
        chatId: String,
        onSuccessful: () -> Unit,
        onExist: () -> Unit,
        onError: () -> Unit,
    ) {
        try {
            val telegramChat = getTelegramChatById(chatId = chatId)
            val twitterAccounts = telegramChat!!.twitterUsers.map { it.username }
            if(twitterAccounts.first{ it == username }.isEmpty()){
                val twitterAccount = TwitterAccount(username = username)
                telegramChat.twitterUsers.add(twitterAccount)
                twitterAccount.chats.add(telegramChat)
                onSuccessful.invoke()
            }
            else onExist.invoke()
        }
        catch (exception: Exception){
            onError.invoke()
        }

    }

    fun addNewChat(chatId: String, onSuccessful: () -> Unit, onError: () -> Unit) {
        try {
            val existChat = telegramChatRepository.findById(chatId).orElse(null)
            if (existChat != null) {
                val chat = TelegramChat(chatId)
                telegramChatRepository.save(chat)
            }
            onSuccessful.invoke()
        } catch (exception: Exception) {
            onError.invoke()
        }
    }

    fun getTwitterAccountByUsername(username: String): TwitterAccount? {
        return twitterUserRepository.findById(username).orElse(null)
    }

    fun getTelegramChatById(chatId: String): TelegramChat? {
        return telegramChatRepository.findById(chatId).orElse(null)
    }
}
