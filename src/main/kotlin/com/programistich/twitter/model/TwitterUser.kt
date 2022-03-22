package com.programistich.twitter.model

import javax.persistence.*

@Entity
@Table(name = "twitter_users")
class TwitterUser(
    @Id
    @Column(name = "username")
    val username: String = "",

    @Column(name = "last_tweet")
    var lastTweetId: Long? = 0,

    @Column(name = "last_like")
    var lastLikeId: Long? = 0,

    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = [
            CascadeType.PERSIST,
            CascadeType.MERGE
        ]
    )
    @JoinTable(
        name = "twitter_chats",
        joinColumns = [JoinColumn(name = "twitter_username")],
        inverseJoinColumns = [JoinColumn(name = "chat_id")]
    )
    var chats: MutableSet<TelegramChat> = hashSetOf()
)
