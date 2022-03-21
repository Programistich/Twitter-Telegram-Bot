package com.programistich.twitterx_bot.entity

import javax.persistence.*

@Entity
@Table(name = "chats")
class TelegramChat(
    @Id
    @Column(name = "chat_id")
    val chatId: String = "",
    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = [
            CascadeType.PERSIST,
            CascadeType.MERGE
        ],
        mappedBy = "chats"
    )
    var twitterUsers: MutableSet<TwitterAccount> = hashSetOf()
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TelegramChat

        if (chatId != other.chatId) return false

        return true
    }

    override fun hashCode(): Int {
        return chatId.hashCode()
    }
}
