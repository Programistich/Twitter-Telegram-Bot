package com.programistich.twitter.service.telegram

import com.programistich.twitter.common.Extensions.id
import com.programistich.twitter.common.TypeCommand
import com.programistich.twitter.model.TelegramChat
import com.programistich.twitter.model.TwitterUser
import com.programistich.twitter.service.db.DatabaseTelegramChatService
import com.programistich.twitter.service.db.DatabaseTwitterUserService
import com.programistich.twitter.service.stocks.StocksService
import com.programistich.twitter.service.twitter.TwitterClientService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import twitter4j.TwitterException

@Service
@Transactional
class DefaultTelegramCommandService(
    private val dataBaseTelegramChatService: DatabaseTelegramChatService,
    private val dataBaseTwitterUserServiceDefault: DatabaseTwitterUserService,
    private val twitterClientService: TwitterClientService,
    private val telegramExecutorService: TelegramExecutorService,
    private val stocksService: StocksService
) : TelegramCommandService {

    private val logger = LoggerFactory.getLogger(DefaultTelegramCommandService::class.java)

    override fun startCommand(chatId: String) {
        val existChat = dataBaseTelegramChatService.existChat(chatId)
        if (existChat) {
            telegramExecutorService.sendTextMessage(chatId, "Этот чат уже есть в БД")
            logger.info("Chat $chatId exist in DB")
        } else {
            dataBaseTelegramChatService.createChat(TelegramChat(chatId))
            telegramExecutorService.sendTextMessage(chatId, "Этот чат добавлен в БД")
            logger.info("Chat $chatId not found in db and register now")
        }
    }

    override fun newTwitterUserCommand(message: Message, username: String?) {
        val chatId = message.id()
        val messageId = message.messageId
        if (username.isNullOrEmpty()) {
            telegramExecutorService.sendTextMessage(chatId, "Поле не может быть пустым")
            telegramExecutorService.deleteMessage(chatId, messageId)
            return
        }
        val existUsernameInTwitterClient = twitterClientService.existUsernameInTwitter(username)
        val existUsernameInChat = dataBaseTelegramChatService.existUsernameTwitter(chatId, username)
        val existUsernameInTwitterDb = dataBaseTwitterUserServiceDefault.existUser(username)
        if (!existUsernameInTwitterClient) {
            telegramExecutorService.sendTextMessage(chatId, "К сожалению такого человека нет в твиттере", messageId)
            logger.info(
                "Chat id = $chatId intent subscribe " +
                        "on $username but this username not exists on Twitter"
            )
            return
        }
        if (existUsernameInChat) {
            telegramExecutorService.sendTextMessage(chatId, "$username уже есть в подписках", messageId)
            logger.info("Username = $username exist in chat")
            return
        }
        val chat = TelegramChat(chatId)
        val twitterUser: TwitterUser? = if (existUsernameInTwitterDb) {
            telegramExecutorService.sendTextMessage(chatId, "Вы добавили $username к себе в чат!", messageId)
            logger.info("Username = $username exist in db")
            dataBaseTwitterUserServiceDefault.getTwitterUserByUsername(username)
        } else {
            telegramExecutorService.sendTextMessage(chatId, "Вы добавили $username к себе в чат!", messageId)
            logger.info("Username = $username NOT exist in db")
            TwitterUser(username)
        }
        if (twitterUser == null) {
            logger.info("Twitter user null")
            return
        }
        twitterUser.lastLikeId = twitterClientService.lastLikeTweetByUsername(username).id
        chat.twitterUsers.add(twitterUser)
        twitterUser.chats.add(chat)
        dataBaseTelegramChatService.updateChat(chat)
        lastLikeTweetByUsernameCommand(message, username)
        logger.info("Username = $username add to chat id = $chatId")
    }

    override fun lastLikeTweetByUsernameCommand(message: Message, username: String) {
        val chatId = message.id()
        val messageId = message.messageId
        val idLast = dataBaseTwitterUserServiceDefault.lastLikeByUsername(username)
        if (idLast == null) {
            logger.info("last tweet by $username not found")
            return
        }
        val typeMessage = twitterClientService.parseTweet(idLast)
        val typeTweet = TypeCommand.Like(username, idLast, true)
        telegramExecutorService.sendTweet(chatId, typeMessage, typeTweet, messageId)
        logger.info("Send message $idLast to $chatId")
    }

    override fun pingCommand(chatId: String) {
        telegramExecutorService.sendTextMessage(chatId, "Живой я, живой, чо пингуешь")
    }

    override fun getTweetCommand(message: Message, link: String?) {
        val author = message.from.firstName
        val chatId = message.chatId.toString()
        val messageId = message.replyToMessage?.messageId
        if (link.isNullOrEmpty()) {
            telegramExecutorService.sendTextMessage(chatId, "Поле не может быть пустым", messageId)
            telegramExecutorService.deleteMessage(chatId, message.messageId)
            return
        }
        try {
            val formatLink = link.replace("https://twitter.com/", "").split("/")
            val username = formatLink[0]
            val idTmp = formatLink[2]
            var result = ""
            for (char in idTmp) {
                if (!char.isDigit()) break
                else result += char
            }
            val id = result.toLong()
            logger.info("get post with id = $id")
            val typeMessage = twitterClientService.parseTweet(id)
            val typeTweet = TypeCommand.Get(username, link, author)
            telegramExecutorService.sendTweet(chatId, typeMessage, typeTweet, messageId)
            telegramExecutorService.deleteMessage(chatId, message.messageId)
        } catch (e: TelegramApiException) {
            logger.info("Error " + e.message)
            telegramExecutorService.sendTextMessage(
                chatId,
                "Что-то пошло не так и Дуров не дает загрузить",
                messageId
            )
        } catch (e: TwitterException) {
            logger.info("Error " + e.message)
            telegramExecutorService.sendTextMessage(
                chatId,
                "Что-то пошло не так и Твиттер что-то не так сделаль",
                messageId
            )
        } catch (e: IndexOutOfBoundsException) {
            logger.info("Error " + e.message)
            telegramExecutorService.sendTextMessage(
                chatId,
                "Что-то пошло не так, точно правильная ссылка?",
                messageId
            )
        }
    }


    override fun donateCommand(message: Message) {
        val chatId = message.id()
        val messageId = message.messageId
        telegramExecutorService.sendTextMessage(
            chatId,
            "Есть несколько вариантов поддержки:\n<a href=\"https://send.monobank.ua/jar/9B7DzKsjk7\">Банка монобанка</a>\nКарта Моно <pre>4441114440821211</pre>\nКарта YooMoney <pre>4048025000163488</pre>\nКрипта <pre>0xf6A2255f333EF47845BaCfD26A0bEaaD296B9019</pre>"
        )
        telegramExecutorService.sendStickerMessage(
            chatId,
            "CAACAgUAAxkBAAIC42HDbkEHhXo4h-g1rQoFfxqdYVjeAAIKAQACdflYFOEIo5rse7wLIwQ"
        )
        telegramExecutorService.deleteMessage(chatId, messageId)
    }

    override fun helpCommand(message: Message) {
        val chatId = message.id()
        val messageId = message.messageId
        telegramExecutorService.sendTextMessage(
            chatId,
            """
                Этот бот предназначен для отслеживания людей(пока что лайки) в твиттере
                • Отправьте ссылку на твит и бот пришлет вам его обратно в нормальном формате!
                • /new %username% - подписка на лайки от конкретного человека 
                • /donate - поддержать автора @programistich
                • /ping - проверка жив ли бот
            """.trimIndent()
        )
    }

    override fun stocksCommand(message: Message) {
        val chatId = message.id()
        val messageId = message.messageId
        val stock = stocksService.getStock("TSLA")
        stocksService.sendStock(chatId, messageId, stock)
    }


}