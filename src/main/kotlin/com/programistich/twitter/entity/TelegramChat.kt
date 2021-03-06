package com.programistich.twitter.entity

import javax.persistence.*

@Entity
@Table(name = "chats")
class TelegramChat(
    @Id
    @Column(name = "chat_id")
    val chatId: String = "",
    @Column(name = "is_channel")
    val isChannel: Boolean,
    @ManyToMany(
        fetch = FetchType.EAGER,
        cascade = [
            CascadeType.PERSIST,
            CascadeType.MERGE
        ],
        mappedBy = "chats"
    )
    var twitterUsers: MutableSet<TwitterUser> = hashSetOf()
)

