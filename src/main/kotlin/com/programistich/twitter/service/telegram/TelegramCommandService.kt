package com.programistich.twitter.service.telegram

import com.programistich.twitter.common.TypeByTweet
import com.programistich.twitter.model.TelegramChat
import com.programistich.twitter.model.TwitterUser
import com.programistich.twitter.service.db.DatabaseTelegramChatService
import com.programistich.twitter.service.db.DatabaseTwitterUserService
import com.programistich.twitter.service.twitter.TwitterClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import twitter4j.TwitterException

@Service
@Transactional
class TelegramCommandService(
    private val dataBaseTelegramChatService: DatabaseTelegramChatService,
    private val dataBaseTwitterUserService: DatabaseTwitterUserService,
    private val twitterClientService: TwitterClientService,
    private val telegramBotExecutorService: TelegramBotExecutorService,
) : DefaultTelegramCommandService {

    private val logger = LoggerFactory.getLogger(TelegramCommandService::class.java)

    override fun registerChat(chatId: String) {
        val existChat = dataBaseTelegramChatService.existChat(chatId)
        if (!existChat) {
            dataBaseTelegramChatService.createChat(TelegramChat(chatId))
            telegramBotExecutorService.sendTextMessage(chatId, "Этот чат добавлен в БД")
            logger.info("Chat $chatId not found in db and register now")
        } else {
            telegramBotExecutorService.sendTextMessage(chatId, "Этот чат уже есть в БД")
            logger.info("Chat $chatId exist in DB")
        }
    }

    override fun addTwitterUsernameToChat(chatId: String, username: String) {
        val existUsernameInTwitterClient = twitterClientService.existUsernameInTwitter(username)
        val existUsernameInChat = dataBaseTelegramChatService.existUsernameTwitter(chatId, username)
        val existUsernameInTwitterDb = dataBaseTwitterUserService.existUser(username)
        if (!existUsernameInTwitterClient) {
            telegramBotExecutorService.sendTextMessage(chatId, "К сожалению такого человека нет в твиттере")
            logger.info(
                "Chat id = $chatId intent subscribe " +
                        "on $username but this username not exists on Twitter"
            )
            return
        }
        if (existUsernameInChat) {
            telegramBotExecutorService.sendTextMessage(chatId, "Этот человек уже есть у вас в чате")
            logger.info("Username = $username exist in chat")
            return
        }
        val chat = TelegramChat(chatId)
        val twitterUser: TwitterUser? = if (existUsernameInTwitterDb) {
            telegramBotExecutorService.sendTextMessage(
                chatId,
                "Этот человек уже есть в БД, но теперь есть и у вас в чате"
            )
            logger.info("Username = $username exist in db")
            dataBaseTwitterUserService.getTwitterUserByUsername(username)
        } else {
            telegramBotExecutorService.sendTextMessage(chatId, "Этот человек новый и для чата и для БД")
            logger.info("Username = $username NOT exist in db")
            TwitterUser(username)
        }
        if (twitterUser == null) {
            telegramBotExecutorService.sendTextMessage(chatId, "Что-то пошло не так в TelegramCommandService")
            logger.info("Twitter user null")
            return
        }
        twitterUser.lastLikeId = twitterClientService.lastLikeTweetByUsername(username).id
        chat.twitterUsers.add(twitterUser)
        twitterUser.chats.add(chat)
        dataBaseTelegramChatService.updateChat(chat)
        lastLikeTweetByUsername(chatId, username)
        logger.info("Username = $username add to chat id = $chatId")
    }

    override fun lastLikeTweetByUsername(chatId: String, username: String) {
        val idLast = dataBaseTwitterUserService.lastLikeByUsername(username)
        if (idLast == null) {
            logger.info("last tweet by $username not found")
            return
        }
        val typeMessage = twitterClientService.parseTweet(idLast)
        val typeTweet = TypeByTweet.Like(username, idLast, true)
        telegramBotExecutorService.sendTweet(chatId, typeMessage, typeTweet)
        logger.info("Send message $idLast to $chatId")
    }

    override fun pingChat(chatId: String) {
        telegramBotExecutorService.sendTextMessage(chatId, "Живой я, живой, чо пингуешь")
    }

    // https://twitter.com/TOSHIK113/status/1472956899146543105?s=20
    // TOSHIK113/status/1472956899146543105?s=20
    // 1472956899146543105?s=20
    override fun getTweet(chatId: String, link: String) {
        val formatLink = link.replace("https://twitter.com/", "").split("/")
        val username = formatLink[0]
        val idTmp = formatLink[2]
        var result = ""
        for (char in idTmp) {
            if (!char.isDigit()) break
            else result += char
        }
        val id = result.toLong()
        try {
            logger.info("get post with id = $id")
            val typeMessage = twitterClientService.parseTweet(id)
            val typeTweet = TypeByTweet.Get(link)
            telegramBotExecutorService.sendTweet(chatId, typeMessage, typeTweet)
        } catch (e: TelegramApiException) {
            logger.info("Error " + e.message)
            telegramBotExecutorService.sendTextMessage(chatId, "Что-то пошло не так и Дуров не дает загрузить")
        } catch (e: TwitterException) {
            logger.info("Error " + e.message)
            telegramBotExecutorService.sendTextMessage(chatId, "Что-то пошло не так и Твиттер что-то не так сделаль")
        }
    }


}