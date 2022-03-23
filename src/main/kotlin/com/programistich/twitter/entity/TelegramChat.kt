package com.programistich.twitter.entity

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
    var twitterUsers: MutableSet<TwitterUser> = hashSetOf()
)

